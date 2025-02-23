package dev.meetree.brainfuck.interpreter

import cats.{Id, Monad}
import cats.data.EitherT
import cats.syntax.all.*

import dev.meetree.brainfuck.core.*

trait Interpreter[F[_]]:
  def interpret(program: Program): F[Either[InterpretError, Tape[Byte]]]

object Interpreter:
  transparent inline def apply[F[_]](using Interpreter[F]): Interpreter[F] = summon

  given id: Interpreter[Id] = make

  def make[F[_]: Monad: InputOutput]: Interpreter[F] =
    program => eval(Tape.const[Byte](0), program).value

  private[interpreter] def eval[F[_]: Monad: InputOutput](
      tape: Tape[Byte],
      program: Program
  ): EitherT[F, InterpretError, Tape[Byte]] =
    program.commands.foldM(tape)(eval)

  private[interpreter] def eval[F[_]](
      tape: Tape[Byte],
      cmd: Command
  )(using io: InputOutput[F], m: Monad[F]): EitherT[F, InterpretError, Tape[Byte]] =
    cmd match
      case Command.IncPtr     => EitherT.fromEither(tape.next.leftMap(InterpretError.Tape(_)))
      case Command.DecPtr     => EitherT.fromEither(tape.prev.leftMap(InterpretError.Tape(_)))
      case Command.IncByte    => EitherT.rightT(tape.inc)
      case Command.DecByte    => EitherT.rightT(tape.dec)
      case Command.OutputByte => EitherT(io.write(tape.get)).leftMap(InterpretError.IO(_)).as(tape)
      case Command.InputByte  => EitherT(io.read).leftMap(InterpretError.IO(_)).map(_.fold(tape)(tape.set))
      case Command.Loop(cmds) => tape.iterateWhileM(eval(_, cmds))(_.get != 0)

enum InterpretError(msg: String) extends Throwable(msg):
  case Tape(err: TapeError)      extends InterpretError(s"Tape: $err")
  case IO(err: InputOutputError) extends InterpretError(s"IO: $err")
