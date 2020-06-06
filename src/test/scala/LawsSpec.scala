package zio.arrow

import zio.test.Assertion._
import zio.test._

object LawsSpec extends ZIOBaseSpec {
  def spec = suite("LawsSpec")(
    suite("Check if 9 `Arrow` laws work for `ZArrow` correctly")(
      testM("Law1: left unit") {
        checkM(anyF) { f =>
          for {
            left  <- laws.law1L(f)
            right <- laws.law1R(f)
            // _     = println(s"Left: ${left}, right: ${right}")
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law2: right unit") {
        checkM(anyF) { f =>
          for {
            left  <- laws.law2L(f)
            right <- laws.law2R(f)
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law3: arrow composition is associative") {
        checkM(anyF, anyF, anyF) { (f, g, h) =>
          for {
            left  <- laws.law3L(f, g, h)
            right <- laws.law3R(f, g, h)
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law4: composition of functions promotes to composition of arrows") {
        checkM(anyF, anyF) { (f, g) =>
          for {
            left  <- laws.law4L(f, g)
            right <- laws.law4R(f, g)
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law5: first on pure arrows rewrites to a pure arrow") {
        checkM(anyF) { (f) =>
          for {
            left  <- laws.law5L(f)
            right <- laws.law5R(f)
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law6: first is a homomorphism for composition ") {
        checkM(anyF, anyF) { (f, g) =>
          for {
            left  <- laws.law6L(f, g)
            right <- laws.law6R(f, g)
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law7: first commutes with a pure arrow that is the identity on the first component of a pair") {
        checkM(anyF, anyF) { (f, g) =>
          for {
            left  <- laws.law7L(f, g)
            right <- laws.law7R(f, g)
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law8: first pushes through promotions of fst") {
        checkM(anyF) { (f) =>
          for {
            left  <- laws.law8L(f)
            right <- laws.law8R(f)
          } yield assert(right)(equalTo(left))
        }
      },
      testM("Law9: first pushes through promotions of assoc ") {
        checkM(anyF) { (f) =>
          for {
            left  <- laws.law9L(f)
            right <- laws.law9R(f)
          } yield assert(right)(equalTo(left))
        }
      }
    )
  )

  val laws = new Laws(Gen.anyInt)
  val anyF = Gen.function(Gen.anyInt)

}
