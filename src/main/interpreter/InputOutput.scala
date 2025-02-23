package dev.meetree.brainfuck.interpreter

import scala.util.Try

import cats.{Applicative, Id}
import cats.syntax.all.*

trait InputOutput[F[_]]:
  def read: F[Either[InputOutputError, Option[Byte]]]
  def write(b: Byte): F[Either[InputOutputError, Unit]]

object InputOutput:
  transparent inline def apply[F[_]](using InputOutput[F]): InputOutput[F] = summon

  given id(using eof: EOF = EOF.NoChange): InputOutput[Id] = make

  def make[F[_]: Applicative](using eof: EOF): InputOutput[F] = new InputOutput[F]:
    def read           = stdin.map(b => if b == -1 then eof.byte else Some(b.toByte)).pure[F]
    def write(b: Byte) = stdout(b.toChar).pure[F]

  private def stdin: Either[InputOutputError, Int] =
    Try(System.in.read).toEither.leftMap(InputOutputError.read)

  private def stdout(ch: Char): Either[InputOutputError, Unit] =
    Try(System.out.print(ch)).toEither.leftMap(InputOutputError.write)

enum InputOutputError(msg: String) extends Throwable(msg):
  case Read(msg: String)  extends InputOutputError(s"Read: $msg")
  case Write(msg: String) extends InputOutputError(s"Write: $msg")

object InputOutputError:
  inline def read(e: Throwable): InputOutputError  = InputOutputError.Read(e.getMessage)
  inline def write(e: Throwable): InputOutputError = InputOutputError.Write(e.getMessage)

enum EOF:
  /** Return 0 on EOF. */
  case Zero
  /** Return -1 (255 as byte) on EOF. */
  case MinusOne
  /** Leave unchanged on EOF. */
  case NoChange

  def byte: Option[Byte] = this match
    case Zero     => Some(0)
    case MinusOne => Some(-1)
    case NoChange => None
