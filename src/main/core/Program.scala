package dev.meetree.brainfuck.core

import cats.Show
import cats.syntax.foldable.*

type Program = Program.Type
object Program:
  opaque type Type = List[Command]

  final val empty: Program = List.empty

  inline def apply(cmds: Command*): Program      = cmds.toList
  inline def apply(cmds: List[Command]): Program = cmds

  extension (p: Program) def commands: List[Command] = p

  given Show[Program] = Show.show[List[Command]](cmds => cmds.mkString_(""))
