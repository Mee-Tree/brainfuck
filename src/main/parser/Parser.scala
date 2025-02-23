package dev.meetree.brainfuck.parser

import scala.annotation.tailrec

import cats.{Applicative, Id}
import cats.data.EitherT
import cats.syntax.all.*

import dev.meetree.brainfuck.core.{Command, Program}

trait Parser[F[_]]:
  def parse(str: String): F[Either[ParseError, Program]]

object Parser:
  transparent inline def apply[F[_]](using Parser[F]): Parser[F] = summon

  given id: Parser[Id] = make

  def make[F[_]: Applicative]: Parser[F] = str =>
    type Commands = List[Command]

    @tailrec
    def go(chars: List[Char], acc: Commands, stack: List[Commands]): EitherT[F, ParseError, Commands] =
      (chars, stack) match
        case (Nil, Nil)            => EitherT.rightT(acc.reverse)
        case (Nil, _)              => EitherT.leftT(ParseError.UnmatchedBracket)
        case (']' :: rest, Nil)    => EitherT.leftT(ParseError.UnmatchedBracket)
        case (']' :: rest, h :: t) => go(rest, Command.loop(acc.reverse) :: h, t)
        case ('[' :: rest, _)      => go(rest, Nil, acc :: stack)
        case ('>' :: rest, _)      => go(rest, Command.IncPtr :: acc, stack)
        case ('<' :: rest, _)      => go(rest, Command.DecPtr :: acc, stack)
        case ('+' :: rest, _)      => go(rest, Command.IncByte :: acc, stack)
        case ('-' :: rest, _)      => go(rest, Command.DecByte :: acc, stack)
        case ('.' :: rest, _)      => go(rest, Command.OutputByte :: acc, stack)
        case (',' :: rest, _)      => go(rest, Command.InputByte :: acc, stack)
        case (_ :: rest, _)        => go(rest, acc, stack)

    go(str.toList, Nil, Nil).fmap(Program(_)).value

  /* Used by CommandLineParser. */
  @throws[ParseError]
  def parseOrThrow(str: String): Program = id.parse(str).fold(err => throw err, identity)

enum ParseError(msg: String) extends IllegalArgumentException(msg):
  case UnmatchedBracket extends ParseError("Unmatched bracket")
