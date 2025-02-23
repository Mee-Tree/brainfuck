package dev.meetree.brainfuck.core

import cats.Show
import cats.syntax.show.*

enum Command:
  /** Increment the data pointer by one (to point to the next cell to the right). */
  case IncPtr
  /** Decrement the data pointer by one (to point to the next cell to the left). */
  case DecPtr
  /** Increment the byte at the data pointer by one. */
  case IncByte
  /** Decrement the byte at the data pointer by one. */
  case DecByte
  /** Output the byte at the data pointer. */
  case OutputByte
  /** Accept one byte of input, storing its value in the byte at the data pointer. */
  case InputByte
  /** If the byte at the data pointer is nonzero, then jump it back to the command at the beginning of the loop. */
  case Loop(cmds: Program)

object Command:
  given Show[Command] = _ match
    case IncPtr     => ">"
    case DecPtr     => "<"
    case IncByte    => "+"
    case DecByte    => "-"
    case OutputByte => "."
    case InputByte  => ","
    case Loop(cmds) => show"[$cmds]"

  inline def loop(cmds: Command*): Command.Loop      = Command.Loop(Program(cmds*))
  inline def loop(cmds: List[Command]): Command.Loop = Command.Loop(Program(cmds))
