package zio.arrow

/**
 * Laws are taken from "The Arrow Calculus", Sam Lindley, Philip Wadler and Jeremy Yallop, Cambridge 2010
 *
 * Their work is based on Paterson "A New Notation on Arrows"
 */
// Types A, B, C ::= · · · | A ~> B

// Constants:
// arr : (A → B) → (A ~> B)
// (>>>) : (A ~> B) → (B ~> C) → (A ~> C)
// first : (A ~> B) → (A×C ; B×C)

// Definitions
// second : (A ~> B) → (C×A ; C×B)
// (&&&) : (C ~> A) → (C ~> B) → (C ; A×B)
//  assoc ((a,b),c) = (a,(b,c))

// ***** Classic Arrow Laws ****** //

// ~>1 : arr id >>> f = f
// ~>2 : f >>> arr id = f
// ~>3 : (f >>> g) >>> h = f >>> (g >>> h)
// ~>4 : arr(f.g) = arr(f) >>> arr (g)
// ~>5 : first(arr.f) = arr (f x id)
// ~>6 : first(f >>> g) = first(f) >>> first(g)
// ~>7 : first f >>> arr (id x g) = arr (id x g) >>> first f
// ~>8 : first f >>> arr fst = arr fst >>> f
// ~>9 : first(first f) >>> arr assoc = arr assoc >>> first f

class Laws[Z](seed: Z) {

  private[this] def arr[P, Q](f: P => Q) = ZArrow(f)

  private[this] def tensor[A, B, C, D](p: A => B, q: C => D): (A, C) => (B, D) = { (a, c) =>
    val b = p(a)
    val d = q(c)
    (b, d)
  }

  private[this] def assoc[A, B, C](din: ((A, B), C)): (A, (B, C)) = {
    val a = din._1._1
    val b = din._1._2
    val c = din._2

    (a, (b, c))
  }

  // ~>1 : arr id >>> f = f

  def law1L[A, B](f: A => B) = (arr(identity[A] _) >>> arr(f)).run(seed.asInstanceOf[A])
  def law1R[A, B](f: A => B) = arr(f).run(seed.asInstanceOf[A])

  // ~>2 : f >>> arr id = f

  def law2L[A, B](f: A => B) = (arr(f) >>> arr(identity[B] _)).run(seed.asInstanceOf[A])
  def law2R[A, B](f: A => B) = arr(f).run(seed.asInstanceOf[A])

  // ~>3 : (f >>> g) >>> h = f >>> (g >>> h)

  def law3L[A, B, C, D](f: A => B, g: B => C, h: C => D) =
    ((arr(f) >>> arr(g)) >>> arr(h)).run(seed.asInstanceOf[A])
  def law3R[A, B, C, D](f: A => B, g: B => C, h: C => D) =
    (arr(f) >>> (arr(g) >>> arr(h))).run(seed.asInstanceOf[A])

  // ~>4 : arr(f.g) = arr(f) >>> arr (g)

  def law4L[A, B, C](f: A => B, g: B => C) = (arr(f.andThen(g))).run(seed.asInstanceOf[A])
  def law4R[A, B, C](f: A => B, g: B => C) = (arr(f) >>> arr(g)).run(seed.asInstanceOf[A])

  // ~>5 : first(arr.f) = arr (f x id)

  def law5L[A, B](f: A => B) = arr(f).first.run((seed.asInstanceOf[A], seed.asInstanceOf[A]))
  def law5R[A, B](f: A => B) =
    arr(tensor(f, identity[A]).tupled).run((seed.asInstanceOf[A], seed.asInstanceOf[A]))

  // ~>6 : first(f >>> g) = first(f) >>> first(g)

  def law6L[A, B, C](f: A => B, g: B => C) =
    (arr(f) >>> arr(g)).first[A].run((seed.asInstanceOf[A], seed.asInstanceOf[A]))

  def law6R[A, B, C](f: A => B, g: B => C) =
    (arr(f).first[C] >>> arr(g).first[C]).run((seed.asInstanceOf[A], seed.asInstanceOf[C]))

  // ~>7 : first f >>> arr (id x g) = arr (id x g) >>> first f

  def law7L[A, B, C](f: A => B, g: B => C) =
    (arr(f).first >>> arr(tensor(identity[B], g).tupled)).run((seed.asInstanceOf[A], seed.asInstanceOf[B]))

  def law7R[A, B, C](f: A => B, g: B => C) =
    (arr(tensor(identity[A], g).tupled) >>> arr(f).first).run((seed.asInstanceOf[A], seed.asInstanceOf[B]))

  // ~>8 : first f >>> arr fst = arr fst >>> f

  def law8L[A, B](f: A => B) =
    (arr(f).first[A] >>> ZArrow.fst).run((seed.asInstanceOf[A], seed.asInstanceOf[A]))

  def law8R[A, B](f: A => B) =
    (ZArrow.fst >>> arr(f)).run((seed.asInstanceOf[A], seed.asInstanceOf[A]))

  // ~>9 : first(first f) >>> arr assoc = arr assoc >>> first f

  def law9L[A, B, C, D](f: A => B) =
    (arr(f)
      .first[A]
      .first[C] >>> arr(assoc[B, A, C])).run(((seed.asInstanceOf[A], seed.asInstanceOf[A]), seed.asInstanceOf[C]))

  def law9R[A, B, C, D](f: A => B) =
    (arr(assoc[A, B, C]) >>> arr(f).first[(B, C)])
      .run(((seed.asInstanceOf[A], seed.asInstanceOf[B]), seed.asInstanceOf[C]))

}
