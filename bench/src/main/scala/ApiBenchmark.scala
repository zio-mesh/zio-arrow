package bench

import java.util.concurrent.TimeUnit

import Helper._
import org.openjdk.jmh.annotations._

import zio.arrow._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ApiBenchmark {
  @Benchmark
  def lift = ZArrow.lift(plusOne)

  @Benchmark
  def id = ZArrow.identity.run(1)

  @Benchmark
  def compose = (add1 <<< mul2).run(1)

  @Benchmark
  def endThen = (add1 >>> mul2).run(1)

  @Benchmark
  def zipWith = (add1 <*> mul2)(_ -> _).run(1)

  @Benchmark
  def first = add1.first.run((1, 1))

  @Benchmark
  def second = add1.second.run((1, 1))

  @Benchmark
  def merge = (add1 *** mul2).run((1, 1))

  @Benchmark
  def split = (add1 &&& mul2).run(1)

  @Benchmark
  def choice = (add1 ||| mul2).run(Left(1))

  @Benchmark
  def asEffect = add1.asEffect.run((1, 1))

  @Benchmark
  def test = {
    val tester = ZArrow.test(ZArrow.lift[List[Int], Boolean](_.sum > 10))
    tester.run(List(1, 2, 3, 4))
  }

  @Benchmark
  def left = ZArrow.fst[Nothing, Int, String].run((1, "hi"))

  @Benchmark
  def right = ZArrow.snd[Nothing, Int, String].run((1, "hi"))

}

object Helper {
  val plusOne = (_: Int) + 1
  val mulTwo  = (_: Int) * 2

  val add1: ZArrow[Nothing, Int, Int] = ZArrow(plusOne)
  val mul2: ZArrow[Nothing, Int, Int] = ZArrow(mulTwo)
}
