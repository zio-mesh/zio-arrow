package zio.arrow

import zio.{ IO, ZIO }

/**
 * A `ZArrow[E, A, B]` is an effectful function from `A` to `B`, which might
 * fail with an `E`.
 *
 * This is the moral equivalent of `A => IO[E, B]`, which can be obtained with
 * `run`.
 *
 * The main advantage to using `ZArrow` is that it provides you a means of
 * importing an impure function `A => B` into `ZArrow[E, A, B]`, without
 * actually wrapping the result of the function in an `IO` value.
 *
 * This allows the implementation to aggressively fuse operations on impure
 * functions, which in turn can result in significantly higher-performance and
 * far less heap utilization than equivalent approaches modeled with `IO`.
 *
 * The implementation allows you to lift functions from `A => IO[E, B]` into a
 * `ZArrow[E, A, B]`. Such functions cannot be optimized, but will be handled
 * correctly and can work in conjunction with optimized (fused) `ZArrow`.
 *
 * Those interested in learning more about modeling effects with `ZArrow` are
 * encouraged to read John Hughes paper on the subject: Generalizing Monads to
 * Arrows (www.cse.chalmers.se/~rjmh/Papers/arrows.pdf). The implementation in
 * this file contains many of the same combinators as Hughes implementation.
 *
 * A word of warning: while even very complex code can be expressed in
 * `ZArrow`, there is a point of diminishing return. If you find yourself
 * using deeply nested tuples to propagate information forward, it may be no
 * faster than using `IO`.
 *
 * Given the following two `ZArrow`:
 *
 * {{{
 * val diceThrow = ZArrow.effectTotal((_: Unit) => scala.util.Random.nextInt(6) + 1)
 * val printLine = ZArrow.effectTotal((line: String) => println(line))
 * }}}
 *
 * Then the following two programs are equivalent:
 *
 * {{{
 * // Program 1
 * val program1: Task[Unit] =
 *   for {
 *     dice <- IO(scala.util.Random.nextInt(6) + 1)
 *     _    <- IO(println("Dice roll: " + dice))
 *   } yield ()
 *
 * // Program 2
 * val program2: Task[Unit] =
 *   (diceThrow >>> ZArrow.effectTotal("Dice roll: " + _.toString) >>> printLine).run(())*
 * }}}
 *
 * Similarly, the following two programs are equivalent:
 *
 * {{{
 * // Program 1
 * val program1: Task[Unit] =
 * for {
 *   dice1 <- IO(scala.util.Random.nextInt(6) + 1)
 *   dice2 <- IO(scala.util.Random.nextInt(6) + 1)
 *   _     <- IO(println("Two dice: " + dice1 + ", " + dice2))
 * } yield ()
 *
 * // Program 2
 * val program2: Task[Unit] =
 *   (diceThrow.zipWith(diceThrow)("Two dice: " + _ + ", " + _) >>> printLine).run(())
 * }}}
 *
 * In both of these examples, the `ZArrow` program is faster because it is
 * able to perform fusion of effectful functions.
 */
sealed trait ZArrow[+E, -A, +B] extends Serializable { self =>

  /**
   * Applies the effectful function with the specified value, returning the
   * output in `IO`.
   */
  val run: A => IO[E, B]

  /**
   * Maps the output of this effectful function by the specified function.
   */
  final def map[C](f: B => C): ZArrow[E, A, C] = self >>> ZArrow.lift(f)

  /**
   * Binds on the output of this effectful function.
   */
  final def flatMap[E1 >: E, A1 <: A, C](f: B => ZArrow[E1, A1, C]): ZArrow[E1, A1, C] =
    ZArrow.flatMap(self, f)

  /**
   * Composes two effectful functions.
   */
  final def compose[E1 >: E, A0](that: ZArrow[E1, A0, A]): ZArrow[E1, A0, B] =
    ZArrow.compose(self, that)

  /**
   * "Backwards" composition of effectful functions.
   */
  final def andThen[E1 >: E, C](that: ZArrow[E1, B, C]): ZArrow[E1, A, C] =
    that.compose(self)

  /**
   * Alias for `andThen`
   */
  final def >>>[E1 >: E, C](that: ZArrow[E1, B, C]): ZArrow[E1, A, C] =
    self.andThen(that)

  /**
   * Alias for `compose`
   */
  final def <<<[E1 >: E, C](that: ZArrow[E1, C, A]): ZArrow[E1, C, B] =
    self.compose(that)

  /**
   * Zips the output of this function with the output of that function, using
   * the specified combiner function.
   */
  final def zipWith[E1 >: E, A1 <: A, C, D](that: ZArrow[E1, A1, C])(f: (B, C) => D): ZArrow[E1, A1, D] =
    ZArrow.zipWith(self, that)(f)

  /**
   * Alias for `zipWith`
   */
  final def <*>[E1 >: E, A1 <: A, C, D](that: ZArrow[E1, A1, C])(f: (B, C) => D): ZArrow[E1, A1, D] = zipWith(that)(f)

  /**
   * Returns a new effectful function that zips together the output of two
   * effectful functions that share the same input.
   */
  final def merge[E1 >: E, A1 <: A, C](that: ZArrow[E1, A1, C]): ZArrow[E1, A1, (B, C)] =
    ZArrow.zipWith(self, that)((a, b) => (a, b))

  /**
   * Alias for `merge`
   */
  final def &&&[E1 >: E, A1 <: A, C](that: ZArrow[E1, A1, C]): ZArrow[E1, A1, (B, C)] = merge(that)

  /**
   * Returns a new effectful function that splits its input between `f` and `g`
   * and combines their output
   */
  final def split[E1 >: E, C, D](that: ZArrow[E1, C, D]): ZArrow[E1, (A, C), (B, D)] =
    (ZArrow.fst[E, A, C] >>> self) &&& (ZArrow.snd[E, A, C] >>> that)

  /**
   * Alias for `split`
   */
  final def ***[E1 >: E, C, D](that: ZArrow[E1, C, D]): ZArrow[E1, (A, C), (B, D)] = split(that)

  /**
   * Returns a new effectful function that computes the value of this function,
   * storing it into the first element of a tuple, carrying along the input on
   * the second element of a tuple.
   */
  final def first[A1]: ZArrow[E, (A, A1), (B, A1)] = self *** ZArrow.identity[A1]

  /**
   * Returns a new effectful function that computes the value of this function,
   * storing it into the second element of a tuple, carrying along the input on
   * the first element of a tuple.
   */
  final def second[A1]: ZArrow[E, (A1, A), (A1, B)] = ZArrow.identity[A1] *** self

  /**
   * Returns a new effectful function that can either compute the value of this
   * effectful function (if passed `Left(a)`), or can carry along any other
   * `C` value (if passed `Right(c)`).
   */
  final def left[C]: ZArrow[E, Either[A, C], Either[B, C]] = ZArrow.left(self)

  /**
   * Returns a new effectful function that can either compute the value of this
   * effectful function (if passed `Right(a)`), or can carry along any other
   * `C` value (if passed `Left(c)`).
   */
  final def right[C]: ZArrow[E, Either[C, A], Either[C, B]] =
    ZArrow.right(self)

  /**
   * Returns a new effectful function that will either compute the value of this
   * effectful function (if passed `Left(a)`), or will compute the value of the
   * specified effectful function (if passed `Right(c)`).
   */
  final def choice[E1 >: E, B1 >: B, C](that: ZArrow[E1, C, B1]): ZArrow[E1, Either[A, C], B1] =
    ZArrow.join(self, that)

  /**
   * Alias for `choice`
   */
  final def |||[E1 >: E, B1 >: B, C](that: ZArrow[E1, C, B1]): ZArrow[E1, Either[A, C], B1] = choice(that)

  /**
   * Maps the output of this effectful function to the specified constant.
   */
  final def as[C](c: => C): ZArrow[E, A, C] =
    self >>> ZArrow.lift[B, C](_ => c)

  /**
   * Converts `ZArrow` into `ZIO`.
   */
  final def toEffect: ZIO[A, E, B] = ZIO.accessM(self.run)

  /**
   * Maps the output of this effectful function to `Unit`.
   */
  final def unit: ZArrow[E, A, Unit] = as(())

  /**
   * Returns a new effectful function that merely applies this one for its
   * effect, returning the input unmodified.
   */
  final def asEffect[A1]: ZArrow[E, (A, A1), A1] = self.first[A1] >>> ZArrow.snd
}

object ZArrow extends Serializable {

  def arr[E, A, B](f: A => B): ZArrow[E, A, B] = lift(f)

  def apply[E, A, B](f: A => B): ZArrow[E, A, B] = lift(f)

  private class ZArrowError[E](error: E) extends Throwable {
    final def unsafeCoerce[E2] = error.asInstanceOf[E2]
  }

  private[zio] final class Pure[+E, -A, +B](val run: A => IO[E, B]) extends ZArrow[E, A, B] {}
  private[zio] final class Impure[+E, -A, +B](val apply0: A => B) extends ZArrow[E, A, B] {
    val run: A => IO[E, B] = a =>
      IO.effectSuspendTotal {
        try {
          val b = apply0(a)
          IO.succeed(b)
        } catch {
          case e: ZArrowError[_] => IO.fail[E](e.unsafeCoerce[E])
        }
      }
  }

  /**
   * Lifts a value into the monad formed by `ZArrow`.
   */
  def succeed[B](b: B): ZArrow[Nothing, Any, B] = lift((_: Any) => b)

  /**
   * Returns a `ZArrow` representing a failure with the specified `E`.
   */
  def fail[E](e: E): ZArrow[E, Any, Nothing] =
    new Impure(_ => throw new ZArrowError[E](e))

  /**
   * Returns the id effectful function, which performs no effects and
   * merely returns its input unmodified.
   */
  def identity[A]: ZArrow[Nothing, A, A] = lift(a => a)

  /**
   * Lifts a pure `A => IO[E, B]` into `ZArrow`.
   */
  def liftM[E, A, B](f: A => IO[E, B]): ZArrow[E, A, B] = new Pure(f)

  /**
   * Converts `ZIO` into `ZArrow`.
   */
  def fromEffect[E, A, B](zio: ZIO[A, E, B]): ZArrow[E, A, B] = liftM(zio.provide)

  /**
   * Lifts a pure `A => B` into `ZArrow`.
   */
  def lift[A, B](f: A => B): ZArrow[Nothing, A, B] = new Impure(f)

  /**
   * Returns an effectful function that merely swaps the elements in a `Tuple2`.
   */
  def swap[E, A, B]: ZArrow[E, (A, B), (B, A)] =
    ZArrow.lift[(A, B), (B, A)](_.swap)

  /**
   * Lifts an impure function into `ZArrow`, converting throwables into the
   * specified error type `E`.
   */
  def effect[E, A, B](catcher: PartialFunction[Throwable, E])(f: A => B): ZArrow[E, A, B] =
    new Impure((a: A) =>
      try f(a)
      catch {
        case t: Throwable if catcher.isDefinedAt(t) =>
          throw new ZArrowError(catcher(t))
      }
    )

  /**
   * Lifts an impure function into `ZArrow`, assuming any throwables are
   * non-recoverable and do not need to be converted into errors.
   */
  def effectTotal[A, B](f: A => B): ZArrow[Nothing, A, B] = new Impure(f)

  /**
   * Returns a new effectful function that passes an `A` to the condition, and
   * if the condition returns true, returns `Left(a)`, but if the condition
   * returns false, returns `Right(a)`.
   */
  def test[E, A](k: ZArrow[E, A, Boolean]): ZArrow[E, A, Either[A, A]] =
    (k &&& ZArrow.identity[A]) >>> ZArrow((t: (Boolean, A)) => if (t._1) Left(t._2) else Right(t._2))

  /**
   * Returns a new effectful function that passes an `A` to the condition, and
   * if the condition returns true, passes the `A` to the `then0` function,
   * but if the condition returns false, passes the `A` to the `else0` function.
   */
  def ifThenElse[E, A, B](
    cond: ZArrow[E, A, Boolean]
  )(then0: ZArrow[E, A, B])(else0: ZArrow[E, A, B]): ZArrow[E, A, B] =
    (cond, then0, else0) match {
      case (cond: Impure[_, _, _], then0: Impure[_, _, _], else0: Impure[_, _, _]) =>
        new Impure[E, A, B](a => if (cond.apply0(a)) then0.apply0(a) else else0.apply0(a))
      case _ => test[E, A](cond) >>> (then0 ||| else0)
    }

  /**
   * Returns a new effectful function that passes an `A` to the condition, and
   * if the condition returns true, passes the `A` to the `then0` function, but
   * otherwise returns the original `A` unmodified.
   */
  def ifThen[E, A](cond: ZArrow[E, A, Boolean])(then0: ZArrow[E, A, A]): ZArrow[E, A, A] =
    (cond, then0) match {
      case (cond: Impure[_, _, _], then0: Impure[_, _, _]) =>
        new Impure[E, A, A](a => if (cond.apply0(a)) then0.apply0(a) else a)
      case _ => ifThenElse(cond)(then0)(ZArrow.identity[A])
    }

  /**
   * Returns a new effectful function that passes an `A` to the condition, and
   * if the condition returns false, passes the `A` to the `then0` function, but
   * otherwise returns the original `A` unmodified.
   */
  def ifNotThen[E, A](cond: ZArrow[E, A, Boolean])(then0: ZArrow[E, A, A]): ZArrow[E, A, A] =
    (cond, then0) match {
      case (cond: Impure[_, _, _], then0: Impure[_, _, _]) =>
        new Impure[E, A, A](a => if (cond.apply0(a)) a else then0.apply0(a))
      case _ => ifThenElse(cond)(ZArrow.identity[A])(then0)
    }

  /**
   * Returns a new effectful function that passes an `A` to the condition, and
   * if the condition returns true, passes the `A` through the body to yield a
   * new `A`, which repeats until the condition returns false. This is the
   * `ZArrow` equivalent of a `while(cond) { body }` loop.
   */
  def whileDo[E, A](check: ZArrow[E, A, Boolean])(body: ZArrow[E, A, A]): ZArrow[E, A, A] =
    (check, body) match {
      case (check: Impure[_, _, _], body: Impure[_, _, _]) =>
        new Impure[E, A, A]({ (a0: A) =>
          var a = a0

          val cond   = check.apply0
          val update = body.apply0

          while (cond(a)) {
            a = update(a)
          }

          a
        })

      case _ =>
        lazy val loop: ZArrow[E, A, A] =
          ZArrow.liftM((a: A) =>
            check.run(a).flatMap((b: Boolean) => if (b) body.run(a).flatMap(loop.run) else IO.succeed(a))
          )

        loop
    }

  /**
   * Prints the current value of Arrow
   */
  def arrPrint[A](): ZArrow[Nothing, A, A] = ZArrow.effectTotal { (din: A) =>
    println(din)
    din
  }

  /**
   * Tap inserts a method call inside the arrow
   */
  def tap[E, A, B](f: A => B): ZArrow[Nothing, A, A] = ZArrow.effectTotal { (din: A) =>
    f(din)
    din
  }

  /**
   * Creates an Arrow, which duplicates the input
   */
  def dup[E, A](): ZArrow[E, A, (A, A)] = ZArrow((a: A) => (a, a))

  /**
   * Returns an effectful function that extracts out the first element of a
   * tuple.
   */
  def fst[E, A, B]: ZArrow[E, (A, B), A] = lift[(A, B), A](_._1)

  /**
   * Returns an effectful function that extracts out the second element of a
   * tuple.
   */
  def snd[E, A, B]: ZArrow[E, (A, B), B] = lift[(A, B), B](_._2)

  /**
   * See @ZArrow.flatMap
   */
  def flatMap[E, A, B, C](fa: ZArrow[E, A, B], f: B => ZArrow[E, A, C]): ZArrow[E, A, C] =
    new Pure((a: A) => fa.run(a).flatMap(b => f(b).run(a)))

  /**
   * See ZArrow.compose
   */
  def compose[E, A, B, C](second: ZArrow[E, B, C], first: ZArrow[E, A, B]): ZArrow[E, A, C] =
    (second, first) match {
      case (second: Impure[_, _, _], first: Impure[_, _, _]) =>
        new Impure(second.apply0.compose(first.apply0))

      case _ =>
        new Pure((a: A) => first.run(a).flatMap(second.run))
    }

  /**
   * See ZArrow.zipWith
   */
  def zipWith[E, A, B, C, D](l: ZArrow[E, A, B], r: ZArrow[E, A, C])(
    f: (B, C) => D
  ): ZArrow[E, A, D] =
    (l, r) match {
      case (l: Impure[_, _, _], r: Impure[_, _, _]) =>
        new Impure((a: A) => {
          val b = l.apply0(a)
          val c = r.apply0(a)

          f(b, c)
        })

      case _ =>
        ZArrow.liftM((a: A) =>
          for {
            b <- l.run(a)
            c <- r.run(a)
          } yield f(b, c)
        )
    }

  /**
   * See ZArrow.left
   */
  def left[E, A, B, C](k: ZArrow[E, A, B]): ZArrow[E, Either[A, C], Either[B, C]] =
    k match {
      case k: Impure[E, A, B] =>
        new Impure[E, Either[A, C], Either[B, C]]({
          case Left(a)  => Left(k.apply0(a))
          case Right(c) => Right(c)
        })
      case _ =>
        ZArrow.liftM[E, Either[A, C], Either[B, C]] {
          case Left(a)  => k.run(a).map[Either[B, C]](Left[B, C])
          case Right(c) => IO.succeed[Either[B, C]](Right(c))
        }
    }

  /**
   * See ZArrow.left
   */
  def right[E, A, B, C](k: ZArrow[E, A, B]): ZArrow[E, Either[C, A], Either[C, B]] =
    k match {
      case k: Impure[E, A, B] =>
        new Impure[E, Either[C, A], Either[C, B]]({
          case Left(c)  => Left(c)
          case Right(a) => Right(k.apply0(a))
        })
      case _ =>
        ZArrow.liftM[E, Either[C, A], Either[C, B]] {
          case Left(c)  => IO.succeed[Either[C, B]](Left(c))
          case Right(a) => k.run(a).map[Either[C, B]](Right[C, B])
        }
    }

  /**
   * See ZArrow.|||
   */
  def join[E, A, B, C](l: ZArrow[E, A, B], r: ZArrow[E, C, B]): ZArrow[E, Either[A, C], B] =
    (l, r) match {
      case (l: Impure[_, _, _], r: Impure[_, _, _]) =>
        new Impure[E, Either[A, C], B]({
          case Left(a)  => l.apply0(a)
          case Right(c) => r.apply0(c)
        })

      case _ =>
        ZArrow.liftM[E, Either[A, C], B]({
          case Left(a)  => l.run(a)
          case Right(c) => r.run(c)
        })
    }
}
