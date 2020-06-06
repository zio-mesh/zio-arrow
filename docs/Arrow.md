## Simple Arrow Examples

* `Function1[A,B]`
```scala mdoc:silent
val plusOne: Int => Int = _ + 1
val mulTwo: Int => Int  = _ * 2

val composedF: Int => Int = plusOne.andThen(mulTwo)
println(composedF(2)) // 6
```

* Effect: `A => M[B]`, where M is a Monad. This is also called a `Kleisli Arrow`
```scala mdoc:silent
val stringToString: String => Option[String] = { (s: String) =>
    Some(s)
  }

  val stringToInt: String => Option[Int] = { (s: String) =>
    try {
      Some(s.toInt)
    } catch {
      case _: Exception => None
    }
  }

  val composedArr: String => Option[Int] = { s: String =>
    for {
      str <- stringToString(s)
      res <- stringToInt(str)
    } yield res
  }

  println(stringToInt("1"))    // Some(1)
  println(stringToInt("blah")) // None
  println(composedArr("2"))    // Some(2)

```

If we look carefully on those two examples, we can notice a pattern. 
* The final result `combined` is computed in exactly two steps in both cases. 
* Each step is chained, which means the input of the following method is the output of the previous. 
* Both `combined` methods accept a single input and produce a single output

This simple analogy pushes one to think that both instances of `Function1` and `Kleisli` are just special cases of some more general case. 
This is indeed a case and the "more general case" is called an `Arrow`.