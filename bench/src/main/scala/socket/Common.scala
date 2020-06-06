package bench
import BenchUtils._

/**
 * Randomizer sock App
 */
object randTest extends App {
  val g1 = fromRange(minRange, maxRange)
  println(g1)
}

/**
 * Benchmarks preparation App
 */
object Prepare extends App {
  setup()
}

/**
 * Cleanup App
 */
object Clean extends App {
  clean()
}

/**
 * Result Validation app
 * This validates that all results are consistent
 */
object Validate extends App {
  val sock = new SocketBenchmark()
  val comp = new ComputeBenchmark()

  setup()

  val plainSockRes = sock.plainBench
  val zioSockRes   = sock.zioBench
  val arrSockRes   = sock.arrowBench

  val plainCompRes = comp.plainBench
  val zioCompRes   = comp.zioBench
  val arrCompRes   = comp.arrowBench

  assert(plainSockRes == zioSockRes)
  assert(zioSockRes == arrSockRes)

  assert(plainCompRes == zioCompRes)
  assert(zioCompRes == arrCompRes)

  println("Successfully Validated")

  // println(s"Plain  compute result \t: ${plainSockRes}")
  // println(s"ZIO    compute result \t: ${zioSockRes}")
  // println(s"ZArrow compute result \t: ${arrSockRes}")

}
