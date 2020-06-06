package examples

import zio.arrow.ZArrow
import zio.arrow.ZArrow._
import zio.console.{ putStrLn }

object Hello extends zio.App {

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
  val arrows   = List(arrF, arrG, arrH)
  val composed = arrows.foldLeft(ZArrow.identity[Int])(_ >>> _)

  // Run a single Arrow effect computation
  val prog0 = composed.run(10)

  def run(args: List[String]) = prog0.flatMap(a => putStrLn(a.toString)).exitCode
}
