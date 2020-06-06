package bench

import scala.{ Array, Boolean, Int, Unit }

import zio.UIO
import zio.arrow._

object ZIOArray {

  def bubbleSort[A](lessThanEqual0: (A, A) => Boolean)(array: Array[A]): UIO[Unit] = {

    type IndexValue   = (Int, A)
    type IJIndex      = (Int, Int)
    type IJIndexValue = (IndexValue, IndexValue)

    val lessThanEqual =
      ZArrow.lift[IJIndexValue, Boolean] {
        case ((_, ia), (_, ja)) => lessThanEqual0(ia, ja)
      }

    val extractIJAndIncrementJ = ZArrow.lift[IJIndexValue, IJIndex] {
      case ((i, _), (j, _)) => (i, j + 1)
    }

    val extractIAndIncrementI = ZArrow.lift[IJIndex, Int](_._1 + 1)

    val innerLoopStart = ZArrow.lift[Int, IJIndex]((i: Int) => (i, i + 1))

    val outerLoopCheck: ZArrow[Nothing, Int, Boolean] =
      ZArrow.lift((i: Int) => i < array.length - 1)

    val innerLoopCheck: ZArrow[Nothing, IJIndex, Boolean] =
      ZArrow.lift { case (_, j) => j < array.length }

    val extractIJIndexValue: ZArrow[Nothing, IJIndex, IJIndexValue] =
      ZArrow.effectTotal {
        case (i, j) => ((i, array(i)), (j, array(j)))
      }

    val swapIJ: ZArrow[Nothing, IJIndexValue, IJIndexValue] =
      ZArrow.effectTotal {
        case v @ ((i, ia), (j, ja)) =>
          array.update(i, ja)
          array.update(j, ia)

          v
      }

    val sort = ZArrow
      .whileDo[Nothing, Int](outerLoopCheck)(
        innerLoopStart >>>
          ZArrow.whileDo[Nothing, IJIndex](innerLoopCheck)(
            extractIJIndexValue >>>
              ZArrow.ifNotThen[Nothing, IJIndexValue](lessThanEqual)(swapIJ) >>>
              extractIJAndIncrementJ
          ) >>>
          extractIAndIncrementI
      )
    sort.run(0).unit
  }
}
