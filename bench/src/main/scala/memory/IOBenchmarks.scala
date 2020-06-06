package bench
import zio.{ IO, ZIO }

object IOBenchmarks {

  def repeat[R, E, A](n: Int)(zio: ZIO[R, E, A]): ZIO[R, E, A] =
    if (n <= 1) zio
    else zio *> repeat(n - 1)(zio)

  def verify(cond: Boolean)(message: => String): IO[AssertionError, Unit] =
    ZIO.when(!cond)(IO.fail(new AssertionError(message)))

  class Thunk[A](val unsafeRun: () => A) {
    def map[B](ab: A => B): Thunk[B] =
      new Thunk(() => ab(unsafeRun()))
    def flatMap[B](afb: A => Thunk[B]): Thunk[B] =
      new Thunk(() => afb(unsafeRun()).unsafeRun())
    def attempt: Thunk[Either[Throwable, A]] =
      new Thunk(() =>
        try Right(unsafeRun())
        catch {
          case t: Throwable => Left(t)
        }
      )
  }
  object Thunk {
    def apply[A](a: => A): Thunk[A] = new Thunk(() => a)

    def fail[A](t: Throwable): Thunk[A] = new Thunk(() => throw t)
  }
}
