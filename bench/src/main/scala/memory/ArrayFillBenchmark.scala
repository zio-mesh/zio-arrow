package bench

import java.util.concurrent.TimeUnit

import scala.collection.immutable.Range

import org.openjdk.jmh.annotations._

import zio.{ IO, Runtime }
import zio.arrow._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ArrayFillBenchmark {
  @Param(Array("10000"))
  var size: Int = _

  val rt = Runtime.default

  def createTestArray: Array[Int] = Range.inclusive(1, size).toArray.reverse

  @Benchmark
  def zioArrayFill() = {

    def arrayFill(array: Array[Int]): ZArrow[Nothing, Int, Int] = {
      val condition = ZArrow.lift[Int, Boolean]((i: Int) => i < array.length)

      ZArrow.whileDo[Nothing, Int](condition)(ZArrow.effectTotal[Int, Int] { (i: Int) =>
        array.update(i, i)

        i + 1
      })
    }

    rt.unsafeRun(
      for {
        array <- IO.effectTotal[Array[Int]](createTestArray)
        _     <- arrayFill(array).run(0)
      } yield ()
    )
  }
}
