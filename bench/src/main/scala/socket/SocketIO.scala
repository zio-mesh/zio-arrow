package bench

import zio.{ Runtime }
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import BenchUtils._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class SocketBenchmark {

  val rt = Runtime.default

  @Benchmark
  def plainBench(): Long = {
    val workers: List[Long] = files.map(f => workerIO(f._1))
    val sumPlain: Long      = workers.sum
    sumPlain
  }

  @Benchmark
  def zioBench(): Long = rt.unsafeRun(monWorkersIO)

  @Benchmark
  def arrowBench(): Long = rt.unsafeRun(arrWorkersIO.run(0L))
}
