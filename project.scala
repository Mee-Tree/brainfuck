//> using scala "3.3.5"
//> using jvm 21

//> using options -deprecation -feature -explain
//> using options -Wunused:imports,implicits,privates -Werror
//> using option -Ykind-projector

//> using dep org.typelevel::cats-core:2.13.0
//> using test.dep org.scalatest::scalatest:3.2.19

package dev.meetree.brainfuck

import scala.util.CommandLineParser
import scala.util.chaining.*

import dev.meetree.brainfuck.core.Program
import dev.meetree.brainfuck.interpreter.Interpreter
import dev.meetree.brainfuck.parser.Parser

given CommandLineParser.FromString[Program] = Parser.parseOrThrow(_)

@main def run(program: Program): ExitCode = Interpreter.id
  .interpret(program)
  .fold(err => err.tap(onError).pipe(_ => ExitCode.Failure(1)), _ => ExitCode.Success)

private def onError(err: Throwable) = System.err.println(err.getMessage)

enum ExitCode(code: Int):
  case Success            extends ExitCode(0)
  case Failure(code: Int) extends ExitCode(code)
