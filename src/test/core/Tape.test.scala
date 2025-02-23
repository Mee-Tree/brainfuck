package dev.meetree.brainfuck.core

import cats.Show
import cats.syntax.all.*
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TapeSpec extends AnyFlatSpec with Matchers with EitherValues:

  "Tape.update" should "update the current cell" in {
    val tape = Tape.one(3)
    tape.update(_ * 2).get shouldBe 6
  }

  "Tape.inc/dec" should "cancel each other out" in {
    val tape = Tape.one(3)
    val incd = tape.inc
    incd.get shouldBe 4
    incd.dec.get shouldBe 3

    val decd = tape.dec
    decd.get shouldBe 2
    decd.inc.get shouldBe 3
  }

  "Tape.next" should "move the pointer to the next cell" in {
    val tape  = Tape(1, 2, 3, 4, 5)
    tape.get shouldBe 1
    val moved = tape.next
    moved.value.get shouldBe 2
  }

  it should "move the pointer multiple times" in {
    val tape  = Tape(1, 2, 3, 4, 5)
    val moved = tape.iterateUntilM(_.next)(_.get == 3)
    moved.value.get shouldBe 3
  }

  it should "return an error if the pointer is at the end of the tape" in {
    val tape  = Tape.one(1)
    val moved = tape.next
    moved.left.value shouldBe TapeError.OutOfBounds
  }

  "Tape.prev" should "move the pointer to the previous cell" in {
    val tape  = Tape(Seq(2, 1), 3, Seq(4, 5))
    tape.get shouldBe 3
    val moved = tape.prev
    moved.value.get shouldBe 2
  }

  it should "move the pointer multiple times" in {
    val tape  = Tape(Seq(3, 2, 1), 4, Seq(5))
    val moved = tape.iterateUntilM(_.prev)(_.get == 1)
    moved.value.get shouldBe 1
  }

  it should "return an error if the pointer is at the end of the tape" in {
    val tape  = Tape.one(1)
    val moved = tape.prev
    moved.left.value shouldBe TapeError.OutOfBounds
  }

  "Tape.show" should "display a single element tape correctly" in {
    val tape = Tape.one(1)
    tape.show shouldBe "(1)"
  }

  it should "display a finite tape correctly" in {
    val tape = Tape(1, 2, 3, 4, 5)
    tape.show shouldBe "(1) 2 3 4 5"
  }

  it should "display only show a part of an infinite tape" in {
    val tape  = Tape.bidir(0)
    tape.show shouldBe "... 0 0 0 0 0 0 0 0 0 0 (0) 0 0 0 0 0 0 0 0 0 0 ..."
    val right = Tape.const(0)
    right.show shouldBe "(0) 0 0 0 0 0 0 0 0 0 0 ..."
  }

  it should "respect max parameter" in {
    val tape = Tape(7 to 0 by -1, 8, 9 to 10)
    Tape.show(tape, max = 3) shouldBe "... 5 6 7 (8) 9 10"
  }
