package dev.meetree.brainfuck.parser

import cats.syntax.show.*
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import dev.meetree.brainfuck.core.*

class ParserSpec extends AnyFlatSpec with Matchers with EitherValues:
  val parser = Parser.id
  import parser.parse

  private def check[A](str: String)(f: Program => Unit): Unit =
    f(parse(str).value)

  "Parser.parse" should "parse valid programs" in {
    val empty      = ""
    val simple     = "[->+<]"
    val helloWorld =
      """++++++++
        |[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]
        |>>.>---.+++++++..+++.>>.
        |<-.<.+++.------.--------.>>+.>++.
      """.stripMargin

    check(empty): p =>
      p shouldBe Program.empty
      p.show shouldBe empty

    check(simple): p =>
      p shouldBe
        Program(Command.loop(Command.DecByte, Command.IncPtr, Command.IncByte, Command.DecPtr))
      p.show shouldBe simple

    check(helloWorld): p =>
      p shouldBe a[Program]
      p.show shouldBe helloWorld.replaceAll("\\s+", "")
  }

  it should "detect unmatched brackets" in {
    parse("[").left.value shouldBe ParseError.UnmatchedBracket
    parse("]").left.value shouldBe ParseError.UnmatchedBracket
    parse("[][]]][[").left.value shouldBe ParseError.UnmatchedBracket
  }

  it should "ignore invalid characters" in {
    check("test"): p =>
      p shouldBe Program.empty
      p.show shouldBe ""

    check("[comment]+"): p =>
      p shouldBe Program(Command.loop(), Command.IncByte)
      p.show shouldBe "[]+"
  }

  it should "not overflow the stack" in {
    val depth   = 100000
    val program = "[".repeat(depth) + "]".repeat(depth)

    noException should be thrownBy parse(program)
    parse(program).value shouldBe a[Program]
  }
