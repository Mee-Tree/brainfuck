package dev.meetree.brainfuck.core

import cats.Show
import cats.syntax.either.*
import cats.syntax.foldable.*
import cats.syntax.show.*

case class Tape[A](
    private val left: LazyList[A],
    private val current: A,
    private val right: LazyList[A],
):
  def get: A                     = current
  def set(a: A): Tape[A]         = Tape(left, a, right)
  def update(f: A => A): Tape[A] = set(f(get))

  def inc(using n: Numeric[A]): Tape[A] = update(n.plus(_, n.one))
  def dec(using n: Numeric[A]): Tape[A] = update(n.minus(_, n.one))

  def prev: Either[TapeError, Tape[A]] = left match
    case LazyList() => TapeError.OutOfBounds.asLeft
    case lh #:: lt  => Tape(lt, lh, current #:: right).asRight

  def next: Either[TapeError, Tape[A]] = right match
    case LazyList() => TapeError.OutOfBounds.asLeft
    case rh #:: rt  => Tape(current #:: left, rh, rt).asRight

object Tape:
  def one[A](current: A): Tape[A] =
    Tape(LazyList.empty, current, LazyList.empty)

  def apply[A](current: A, right: A*): Tape[A] =
    Tape(LazyList.empty, current, LazyList.from(right))

  def apply[A](left: Seq[A], current: A, right: Seq[A]): Tape[A] =
    Tape(LazyList.from(left), current, LazyList.from(right))

  def const[A](elem: A): Tape[A] =
    Tape(LazyList.empty, elem, LazyList.continually(elem))

  def bidir[A](elem: A): Tape[A] =
    Tape(LazyList.continually(elem), elem, LazyList.continually(elem))

  /** In case of a large or infinite tape, only show a maximum of `max` values from each side. */
  def show[A: Show](tape: Tape[A], max: Int = 10): String =
    val left    = tape.left.take(max).reverse.map(_.show)
    val right   = tape.right.take(max).map(_.show)
    val prefix  = if left.size < max then "" else "... "
    val postfix = if right.size < max then "" else " ..."
    (left #::: show"(${tape.current})" #:: right).mkString_(prefix, " ", postfix)

  given [A: Show]: Show[Tape[A]] = Tape.show(_)

enum TapeError(msg: String) extends Throwable(msg):
  case OutOfBounds extends TapeError("Out of bounds")
