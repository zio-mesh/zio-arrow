package zio.arrow
import zio.{ URIO }

object SpecUtils {
  val plusOne  = (_: Int) + 1
  val minusOne = (_: Int) - 1
  val mulTwo   = (_: Int) * 2

  val add1: ZArrow[Nothing, Int, Int] = ZArrow(plusOne)
  val min1: ZArrow[Nothing, Int, Int] = ZArrow(minusOne)
  val mul2: ZArrow[Nothing, Int, Int] = ZArrow(mulTwo)

  val zioAdd1: URIO[Int, Int] = URIO.access(plusOne)

  val greaterThan0 = ZArrow((_: Int) > 0)
  val lessThan10   = ZArrow((_: Int) < 10)

  val thrower = ZArrow.effect[String, Int, Int] { case _: Throwable => "error" }(_ => throw new Exception)
}
