package bench

import java.util.concurrent.TimeUnit

import scala.collection.immutable.Range

import org.openjdk.jmh.annotations._

import zio.{ IO, Runtime }

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class BubbleSortBenchmark {
  @Param(Array("1000"))
  var size: Int = _

  val rt = Runtime.default

  def createTestArray: Array[Int] = Range.inclusive(1, size).toArray.reverse
  def assertSorted(array: Array[Int]): Unit =
    if (!array.sorted.sameElements(array)) {
      throw new Exception("Array not correctly sorted")
    }

  @Benchmark
  def zioBubbleSort() = {
    import ZIOArray._

    rt.unsafeRun(
      for {
        array <- IO.effectTotal[Array[Int]](createTestArray)
        _     <- bubbleSort[Int](_ <= _)(array)
        _     <- IO.effectTotal[Unit](assertSorted(array))
      } yield ()
    )
  }
}
