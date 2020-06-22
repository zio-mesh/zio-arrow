package examples

import scalax.collection.Graph
import scalax.collection.GraphEdge._
import scalax.collection.GraphPredef._

import zio.arrow.ZArrow
import zio.arrow.ZArrow._
import zio.console.putStrLn

case class MyNode(id: Int, op: ZArrow[Nothing, Int, Int])

// case class MyEdge(
//   val nodeFrom: MyNode,
//   val nodeTo: MyNode
// ) extends DiEdge(NodeProduct(nodeFrom, nodeTo))

object GraphApp extends zio.App {

  def run(args: List[String]) = (prog1 >>= (v => putStrLn(v.toString))).exitCode

  val f = (_: Int) + 1
  val g = (_: Int) * 2
  val h = (_: Int) - 3

  val arrF = arr(f)
  val arrG = arr(g)
  val arrH = arr(h)

  val n1 = MyNode(1, arrF)
  val n2 = MyNode(2, arrG)
  val n3 = MyNode(3, arrH)

  val nset = Set(n1, n2, n3)
  val eset = Set(n1 ~> n2, n2 ~> n3)
  val g1   = Graph.from(nset, eset)

  println("*** Graph Properties ***")
  println(s"Is acyclic: ${g1.isAcyclic}")
  println(s"Is complete: ${g1.isComplete}")

  val comp0 = nset.foldLeft(ZArrow.identity[Int])((x, y) => x >>> y.op)
  val comp1 = g1.nodes.foldLeft(ZArrow.identity[Int])((x, y) => x >>> y.op)

  val prog1 = comp1.run(4)
}
