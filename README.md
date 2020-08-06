[![Maven][mavenImg]][mavenLink]

[mavenImg]: https://img.shields.io/maven-central/v/io.github.neurodyne/zio-arrow_2.13.svg
[mavenLink]: https://mvnrepository.com/artifact/io.github.neurodyne/zio-arrow

# Welcome to ZIO Arrow !

Q: What's an `Arrow`? <br>
A: `Arrow` is a monoid in a category of strong profunctors

More info [here](docs/Intro.md) <br>
Join discussions on [Discord](https://discord.com/channels/629491597070827530/671823334421430282)

ZIO Arrow is an effectful data structure for modeling a highly composable effects. As an example of effect composition, consider the following code:

```scala
import zio.{ ZIO }
import zio.arrow.ZArrow._
import zio.console.{ putStrLn }
import zio.arrow.ZArrow

object ArrowMonad extends zio.App {

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
  val arrows = List(arrF, arrG, arrH)

  val arrowComposed: ZArrow[Nothing, Int, Int] = arrows.foldLeft(ZArrow.identity[Int])(_ >>> _)

  // Run an effect computation for a composed Arrow
  val prog0 = arrowComposed.run(10)

  // Compose effects in a monadic context
  def monadComposed(din: Int): ZIO[Any, Nothing, Int] =
    for {
      f0 <- ZIO.effectTotal(f)
      g0 <- ZIO.effectTotal(g)
      h0 <- ZIO.effectTotal(h)
    } yield f0.andThen(g0).andThen(h0).apply(din)

  // Run a cmposed Monad effect computation
  val prog1 = monadComposed(10)

  def run(args: List[String]) = (prog0 <*> prog1).flatMap(a => putStrLn(a.toString)).exitCode
}
// (19,19)
```

Both effects result in the same value. What's different is the performance for such composable effects. Let's look at the decompiled code, obtained with [CFR](http://www.benf.org/other/cfr/)

```bash
  Arrow >>>>>> arrowComposed = (ZArrow)MODULE$.arrows().foldLeft((Object)ZArrow$.MODULE$.identity(), (Function2 & Serializable)(x$4, x$5) -> x$4.$greater$greater$greater(x$5));
  
  Monad >>>>>> public ZIO<Object, Nothing$, Object> monadComposed(int din) {return ZIO$.MODULE$.effectTotal((Function0 & Serializable)() -> <br>MODULE$.f()).flatMap((Function1 & Serializable)f0 -> ZIO$.MODULE$.effectTotal((Function0 & Serializable)() -> MODULE$.g()).flatMap((Function1 & Serializable)g0 -> ZIO$.MODULE$.effectTotal((Function0 & Serializable)() -> MODULE$.h()).map((Function1 & Serializable)h0 -> BoxesRunTime.boxToInteger((int)ArrowMonad$.$anonfun$monadComposed$6(f0, g0, din, h0)))));
```

As we see from a decompiled code, Scala compiler assembles `Arrow` into a single nice static object and folds a `Function2` with and `Identity function` to obtain the final result. On the other hand, `Monad` is implemented as a chained computation with `flatMap`.

Each line in a procedural programming or monadic `for` context costs six! (6) allocations on JVM plus 3 extra megamorphic dispatches in Functional Programming, according to [this talk](https://www.youtube.com/watch?v=L8AEj6IRNEE)

`Arrow` effects cost ZERO! (0) extra allocations and one megamorphic dispatch on JVM.

This is how `Arrows` became the next big thing in a high performance programming on JVM

## Usage 
```scala
libraryDependencies += "io.github.neurodyne" %% "zio-arrow" % "0.2.1"
```
## Credits

Initial contributors: [Wiem Zine El Abidine](https://github.com/wi101), [John De Goes](https://github.com/jdegoes), [Adam Fraser](https://github.com/adamgfraser/)
