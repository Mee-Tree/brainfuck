# Brainfuck

A simple [Brainfuck](https://en.wikipedia.org/wiki/Brainfuck) interpreter implemented in Scala, built primarily for the purpose of trying out [Scala CLI](https://scala-cli.virtuslab.org/).

## Usage

### Running the Project

```bash
# Run the interpreter
scala-cli run . -- "program"

# Run tests
scala-cli test .
```

### Example Programs

You can find example programs in the `examples/` directory and run them using:
```bash
scala-cli run . -- "$(cat path-to-example)"
```
All examples are sourced from [wikipedia.org](https://en.wikipedia.org/wiki/Brainfuck#Examples) and [brainfuck.org](https://brainfuck.org/).

### Commands

The language consists of eight single-character commands:
| Command | Description |
|---------|-------------|
| `>`     | Increment the data pointer by one (to point to the next cell to the right).|
| `<`     | Decrement the data pointer by one (to point to the next cell to the left). |
| `+`     | Increment the byte at the data pointer by one. |
| `-`     | Decrement the byte at the data pointer by one. |
| `.`     | Output the byte at the data pointer. |
| `,`     | Accept one byte of input, storing its value in the byte at the data pointer. |
| `[`     | If the byte at the data pointer is zero, then instead of moving the instruction pointer forward to the next command, jump it forward to the command after the matching `]` command |
| `]`     | If the byte at the data pointer is nonzero, then instead of moving the instruction pointer forward to the next command, jump it back to the command after the matching `[` command. |

All other characters are ignored and can be used for comments.
