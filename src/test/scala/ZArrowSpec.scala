package zio.arrow

import SpecUtils._

import zio.{ IO }
import zio.arrow.ZArrow._
import zio.test.Assertion._
import zio.test._

object ZArrowSpec extends ZIOBaseSpec {

  def spec = suite("ZArrowSpec")(
    suite("Check if the functions in `ZArrow` work correctly")(
      testM("`apply` lifts from A => B into effectful function") {
        val arr = ZArrow(plusOne)
        assertM(arr.run(4))(equalTo(5))
      },
      testM("`lift` lifts from A => B into effectful function") {
        assertM(add1.run(4))(equalTo(5))
      },
      testM("`identity` returns the id of the input without modification") {
        assertM(identity[Int].run(1))(equalTo(1))
      },
      testM("`>>>` is a symbolic operator of `andThen`which does a Backwards composition of effectful functions") {
        assertM((add1 >>> mul2).run(6))(equalTo(14))
      },
      testM("`<<<` is a symbolic operator of `compose` which compose two effectful functions") {
        assertM((add1 <<< mul2).run(6))(equalTo(13))
      },
      testM("`zipWith` zips the output of two effectful functions") {
        assertM((add1 <*> mul2)(_ -> _).run(6))(equalTo(7 -> 12))
      },
      testM("`***` feeds a tuple to the two effectful functions and returns a tuple of their results") {
        assertM((add1 *** mul2).run((3, 3)))(equalTo((4, 6)))
      },
      testM("`&&&` zips the output of two effectful functions and returns a tuple of their result") {
        assertM((add1 &&& mul2).run(6))(equalTo(7 -> 12))
      },
      testM("`|||` computes two effectful functions left and right from from an Either input") {
        for {
          l1 <- (add1 ||| mul2).run(Left(25))
          r1 <- (add1 ||| mul2).run(Right(25))
        } yield assert(l1)(equalTo(26)) &&
          assert(r1)(equalTo(50))
      },
      testM("`first` returns a tuple: the output on the first element and input on the second element") {
        assertM(mul2.first.run((100, 100)))(equalTo(200 -> 100))
      },
      testM("`second` returns a tuple: the input on the first element and output on the second element") {
        assertM(mul2.second.run((100, 100)))(equalTo(100 -> 200))
      },
      testM(
        "`left` takes an Either as input and computes it if it is Left otherwise returns the same value of the input"
      ) {
        for {
          v1 <- mul2.left[Int].run(Left(6))
          v2 <- succeed(1).left[String].run(Right("hi"))
        } yield assert(v1)(isLeft(equalTo(12))) && assert(v2)(isRight(equalTo("hi")))
      },
      testM(
        "`right`takes an Either as input and computes it if it is Right otherwise returns the same value of the input"
      ) {
        for {
          v1 <- mul2.right[String].run(Left("no value"))
          v2 <- mul2.right[Int].run(Right(7))
        } yield assert(v1)(isLeft(equalTo("no value"))) && assert(v2)(isRight(equalTo(14)))
      },
      testM("`asEffect` returns the input value")(
        assertM(mul2.asEffect.run((56, 56)))(equalTo(56))
      ),
      testM("`fromEffect` converts `ZIO` into `ZArrow`")(
        assertM(ZArrow.fromEffect(zioAdd1).run(1))(equalTo(2))
      ),
      testM("`toEffect` converts `ZArrow` into `ZIO`")(
        assertM(add1.toEffect.provide(1))(equalTo(2))
      ),
      testM("`test` check a condition and returns an Either output: Left if the condition is true otherwise false") {
        val tester = ZArrow.test(lift[List[Int], Boolean](_.sum > 10))

        for {
          v1 <- tester.run(List(1, 2, 5))
          v2 <- tester.run(List(1, 2, 5, 6))
        } yield assert(v1)(isRight(equalTo(List(1, 2, 5)))) && assert(v2)(isLeft(equalTo(List(1, 2, 5, 6))))
      },
      suite("`ifThenElse`")(
        testM(
          "check an impure condition if it is true then computes an effectful function `then0` else computes `else0`"
        ) {
          val checker = ifThenElse(greaterThan0)(succeed("is positive"))(succeed("is negative"))

          for {
            v1 <- checker.run(-1)
            v2 <- checker.run(1)
          } yield assert(v1)(equalTo("is negative")) && assert(v2)(equalTo("is positive"))
        },
        testM(
          "check a pure condition if it is true then computes an effectful function `then0` else computes `else0`"
        ) {
          val greaterThan0M = liftM[Nothing, Int, Boolean](a => IO.succeed(a > 0))
          val checker       = ifThenElse(greaterThan0M)(succeed("is positive"))(succeed("is negative"))

          for {
            v1 <- checker.run(-1)
            v2 <- checker.run(1)
          } yield assert(v1)(equalTo("is negative")) && assert(v2)(equalTo("is positive"))
        }
      ),
      suite("`whileDo`")(
        testM("take a condition and run the body until the condition will be  false with impure function") {
          assertM(whileDo[Nothing, Int](lessThan10)(add1).run(1))(equalTo(10))
        },
        testM(
          "take a condition and run the body until the condition will be  false with pure function"
        ) {
          val lestThan10M = liftM[Nothing, Int, Boolean](a => IO.succeed[Boolean](a < 10))
          val add1M       = liftM[Nothing, Int, Int](a => IO.effectTotal[Int](a + 1))

          assertM(whileDo[Nothing, Int](lestThan10M)(add1M).run(1))(equalTo(10))
        }
      ),
      testM("`fst` extracts out the first element of a tuple") {
        assertM(fst[Nothing, Int, String].run((1, "hi")))(equalTo(1))
      },
      testM("`snd` extracts out the second element of a tuple") {
        assertM(snd[Nothing, Int, String].run((1, "hi")))(equalTo("hi"))
      },
      testM("`fail` returns a failure") {
        assertM(ZArrow.fail[String]("error").run(1).either)(isLeft(equalTo("error")))
      },
      testM("`effect` can translate an Exception to an error") {
        assertM(thrower.run(9).either)(isLeft(equalTo("error")))
      },
      testM("`ignore` ignores a effect failure") {
        assertM(thrower.run(9).ignore)(isUnit)
      }
    )
  )
}
