package bench

import zio.{ Runtime }
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import BenchUtils._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ComputeBenchmark {
  val rt = Runtime.default

  @Benchmark
  def plainBench(): Long = {
    val workers: List[Long] = seeds.map(factorial)
    workers.sum
  }

  @Benchmark
  def zioBench(): Long = rt.unsafeRun(monWorkersCompute)

  @Benchmark
  def arrowBench(): Long = rt.unsafeRun(arrWorkersCompute.run(0L))
}
