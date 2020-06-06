package examples

import zio.{ ZIO }
import zio.arrow.ZArrow
import zio.arrow.ZArrow._
import zio.console.{ putStrLn }

object ArrowMonad extends zio.App {

  // Define plain methods
  val f = (_: Int) + 1
  val g = (_: Int) * 2
  val h = (_: Int) - 3

  // Lift methods to an arrow context
  val arrF = arr(f)
  val arrG = arr(g)
  val arrH = arr(h)

  // Compose arrows to the final arrow
  // No computation is performed, only a composition of ZIO Effects in the Arrow context
  val arrows = List(arrF, arrG, arrH)

  val arrowComposed: ZArrow[Nothing, Int, Int] = arrows.foldLeft(ZArrow.identity[Int])(_ >>> _)

  // Run an effect computation for a composed Arrow
  val prog0 = arrowComposed.run(10)

  // Compose effects in a monadic context
  def monadComposed(din: Int): ZIO[Any, Nothing, Int] =
    for {
      f0 <- ZIO.effectTotal(f)
      g0 <- ZIO.effectTotal(g)
      h0 <- ZIO.effectTotal(h)
    } yield f0.andThen(g0).andThen(h0).apply(din)

  // Run a cmposed Monad effect computation
  val prog1 = monadComposed(10)

  def run(args: List[String]) = (prog0 <*> prog1).flatMap(a => putStrLn(a.toString)).exitCode
}
