package dev.meetree.brainfuck.interpreter

import cats.Id
import cats.data.StateT
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import dev.meetree.brainfuck.core.*

class InterpreterSpec extends AnyFlatSpec with Matchers with EitherValues:
  import Interpreter.eval

  type F[A] = StateT[Id, Data, A]

  given eof: EOF = EOF.NoChange
  given InputOutput[F] with
    def read: F[Either[InputOutputError, Option[Byte]]]      = StateT.apply(s => s.read)
    def write(byte: Byte): F[Either[InputOutputError, Unit]] = StateT.apply(s => s.write(byte))

  case class Data(inputs: List[Byte], outputs: List[Byte], faulty: Boolean = false)(using eof: EOF):
    def read: (Data, Either[InputOutputError, Option[Byte]]) =
      inputs match
        case _ if faulty  => (this, Left(InputOutputError.Read("read")))
        case head :: tail => (Data(tail, outputs, faulty), Right(Some(head)))
        case Nil          => (this, Right(eof.byte))

    def write(byte: Byte): (Data, Either[InputOutputError, Unit]) =
      if faulty then (this, Left(InputOutputError.Write("write")))
      else (Data(inputs, byte :: outputs, faulty), Right(()))

  object Data:
    final val empty: Data  = Data(Nil, Nil)
    final val faulty: Data = Data.empty.copy(faulty = true)

  val interpreter = Interpreter.make[F]
  import interpreter.interpret

  "Interpreter.interpret" should "evaluate simple commands" in {
    val program        = Program(
      Command.IncByte,
      Command.IncByte,
      Command.OutputByte,
      Command.IncPtr,
      Command.IncByte,
      Command.OutputByte,
    )
    val (data, result) = interpret(program).run(Data.empty)
    data.outputs shouldBe List(1, 2)
    result.value.get shouldBe 1
    result.value.prev.value.get shouldBe 2
  }

  it should "evaluate programs with input" in {
    val program        = Program(Command.InputByte, Command.IncByte, Command.OutputByte)
    val (data, result) = interpret(program).run(Data(List(5), Nil))
    data.inputs shouldBe Nil
    data.outputs shouldBe List(6)
    result.value.get shouldBe 6
  }

  it should "evaluate programs with loops" in {
    val loop           = Command.loop(Command.IncByte, Command.DecByte, Command.DecByte)
    val program        = Program(Command.IncByte, Command.IncByte, Command.IncByte, loop, Command.OutputByte)
    val (data, result) = interpret(program).run(Data.empty)
    data.outputs shouldBe List(0)
    result.value.get shouldBe 0
  }

  it should "fail on invalid programs" in {
    val tape         = Tape.one[Byte](0)
    val (_, resultd) = eval[F](tape, Program(Command.DecPtr)).value.run(Data.empty)
    resultd.left.value shouldBe InterpretError.Tape(TapeError.OutOfBounds)

    val (_, resulti) = eval[F](tape, Command.IncPtr).value.run(Data.empty)
    resulti.left.value shouldBe InterpretError.Tape(TapeError.OutOfBounds)
  }

  it should "fail on IO errors" in {
    val tape         = Tape.one[Byte](0)
    val (_, resulti) = eval[F](tape, Command.InputByte).value.run(Data.faulty)
    resulti.left.value shouldBe InterpretError.IO(InputOutputError.Read("read"))

    val (_, resulto) = eval[F](tape, Command.OutputByte).value.run(Data.faulty)
    resulto.left.value shouldBe InterpretError.IO(InputOutputError.Write("write"))
  }
