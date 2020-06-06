package bench

import java.util.concurrent.TimeUnit

import zio.{ ZIO }
import zio.arrow._
import FileUtils._
import scala.annotation.tailrec
import java.util.concurrent.ThreadLocalRandom

object BenchUtils {

  val rand = ThreadLocalRandom.current

  /**
   * Bench setup
   * This mimics the number of simultaneous open connections to the  server
   */
  val numIOWorkers      = 10
  val numComputeWorkers = 1000

  // Random seed range for factorial
  val minRange = 8L
  val maxRange = 12L

  /**
   * Generates a random Int from a specific range
   */
  def fromRange(start: Long, end: Long) = start + rand.nextLong((end - start) + 1)

  /**
   * Simple non-stack safe factorial function
   */
  def factorial(n: Long): Long = {
    @tailrec
    def factorialAccumulator(acc: Long, n: Long): Long =
      if (n == 0) acc
      else factorialAccumulator(n * acc, n - 1)
    factorialAccumulator(1, n)
  }

  /**
   * Prepare test file data
   */
  val files = List.tabulate(numIOWorkers)(num => ("file" + num.toString, fromRange(minRange, maxRange).toString))

  val seeds: List[Long] = List.fill(numComputeWorkers)(fromRange(minRange, maxRange))

  /**
   * Create test files
   */
  def setup() = {
    newDir("bench")
    files.foreach { f =>
      newFile(f._1)
      wrFile(f._1, f._2)
    }
  }

  /**
   * Remove test files
   */
  def clean() = files.foreach(f => delFile(f._1))

  /**
   * Impure unsafe workerIO process
   * This performs IO to read the file, gets a value and calculates a `factorial` for that value
   */
  def workerIO(file: String): Long = {
    // this reads a value from file
    val seed = rdFile(file).fold(0L)(data => data.toLong)

    // computes a factorial on the value read
    factorial(seed)
  }

  /**
   * ZIO effect for monoidal computation, which adds a `workerIO` output for every file the list
   */
  val monWorkersIO = for {
    list <- ZIO.foreach(files)(item => ZIO.effect(workerIO(item._1)))
    out  = list.sum
  } yield out

  val monWorkersCompute = for {
    list <- ZIO.foreach(seeds)(seed => ZIO.effectTotal(factorial(seed)))
    out  = list.sum
  } yield out

  /**
   * Composed Arrow Workers, which adds a `workerIO` output for every file the list
   */
  val arrWorkersIO = files.foldLeft(ZArrow.identity[Long]) { (arr, item) =>
    arr >>> ZArrow((acc: Long) => acc + workerIO(item._1))
  }

  val arrWorkersCompute = seeds.foldLeft(ZArrow.identity[Long]) { (arr, seed) =>
    arr >>> ZArrow((acc: Long) => acc + factorial(seed))
  }

  /**
   * Extra utilities for quick latency estimation
   */
  def time[R](block: => R): R = {
    val t0        = System.nanoTime()
    val result    = block // call-by-name
    val runtimeNs = System.nanoTime - t0
    val runtimeUs = TimeUnit.MICROSECONDS.convert(runtimeNs, TimeUnit.NANOSECONDS)
    println("Elapsed time: " + runtimeUs + "us")
    result
  }

  def showTime(runtime: Long): Unit = {
    val runtimeUs = TimeUnit.MICROSECONDS.convert(runtime, TimeUnit.NANOSECONDS)
    println("Total Runtime: " + runtimeUs + "us")
    println()
  }

}
