package dev.meetree.brainfuck.interpreter

import scala.util.Try

import cats.{Applicative, Id}
import cats.syntax.all.*

trait InputOutput[F[_]]:
  def read: F[Either[InputOutputError, Byte]]
  def write(b: Byte): F[Either[InputOutputError, Unit]]

object InputOutput:
  transparent inline def apply[F[_]](using InputOutput[F]): InputOutput[F] = summon

  given id: InputOutput[Id] = make

  def make[F[_]: Applicative]: InputOutput[F] = new InputOutput[F]:
    def read           = stdin.pure[F]
    def write(b: Byte) = stdout(b).pure[F]

  private def stdin: Either[InputOutputError, Byte] =
    Try((0 max System.in.read).toByte).toEither.leftMap(InputOutputError.read)

  private def stdout(b: Byte): Either[InputOutputError, Unit] =
    Try(System.out.print(b.toChar)).toEither.leftMap(InputOutputError.write)

enum InputOutputError(msg: String) extends Throwable(msg):
  case Read(msg: String)  extends InputOutputError(s"Read: $msg")
  case Write(msg: String) extends InputOutputError(s"Write: $msg")

object InputOutputError:
  inline def read(e: Throwable): InputOutputError  = InputOutputError.Read(e.getMessage)
  inline def write(e: Throwable): InputOutputError = InputOutputError.Write(e.getMessage)
