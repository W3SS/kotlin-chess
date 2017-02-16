package com.rrpictureproductions.chess

import com.rrpictureproductions.chess.figures.ChessPiece
import com.rrpictureproductions.chess.figures.Color

val resetRegex = Regex("""reset""", RegexOption.IGNORE_CASE)
val fieldRegex = Regex("""([a-hA-H][1-8])""")
val moveRegex = Regex(fieldRegex.pattern + """\s+(?:to|TO)\s+""" + fieldRegex.pattern)

fun main(args: Array<String>) {
    val board = Board()
    Action.Reset().executeOn(board)
    var currentColor = Color.WHITE
    println()
    board.print()
    println("${currentColor.name}, your turn")
    println()
    readLoop { input ->
        val action = parseAction(input)
        if(action == null) {
            println("I did not understand that. Please try again")
            println()
            skip()
        }
        try {
            action.executeOn(board)
        } catch (e: InvalidMoveException) {
            println("Impossible move: ${e.message}")
            println()
            println("${currentColor.name}, try again")
            println()
            skip()
        }
        currentColor = when(currentColor) { Color.WHITE -> Color.BLACK; Color.BLACK -> Color.WHITE }
        if(action is Action.Reset) currentColor = Color.WHITE
        board.print()
        println("${currentColor.name}, your turn")
        println()
    }
    println("Quitting the game. Bye!")
}

fun parseAction(input: String): Action? {
    if(input.matches(resetRegex)) return Action.Reset()
    val matchResult = moveRegex.matchEntire(input)
    if(matchResult != null) {
        val (start, end) = matchResult.destructured
        return Action.Move(start.toPosition(), end.toPosition())
    }
    return null
}

fun readLoop(exitCommand: String = "exit", block: ReadLoop.(String) -> Unit) {
    System.`in`.bufferedReader().use { input ->
        val loop = ReadLoop()
        var inp = input.readLine()
        while(inp.toLowerCase() != exitCommand.toLowerCase()) {
            try {
                loop.block(inp)
            } catch (e: LoopExecutionSkippedException) {
            } catch (e: LoopExitedException) { break; }
            inp = input.readLine()
        }
    }
}
class ReadLoop {
    fun skip(): Nothing = throw LoopExecutionSkippedException()
    fun exit(): Nothing = throw LoopExitedException()
}
class LoopExecutionSkippedException : Exception()
class LoopExitedException : Exception()

fun Board.print() {
    println()
    println(PRINT_TOP)
    Board.RANKS.reversed().forEach { rank ->
        print("$rank │ ")
        Board.FILES.forEach { file ->
            print("${files[file, rank]?.piece?.toString() ?: PIECE_SPACE} | ")
        }
        println()
        if(rank != Board.RANKS.first) {
            println(PRINT_BET)
        }
    }
    println(PRINT_BOT)
    print(" ")
    Board.FILES.forEach { file ->
        print("  $PIECE_SPACE$file")
    }
    println()
    println()
}

val PIECE_SPACE = "      "
val PIECE_THIRD_SPACE = "  "
val HOR_LINE = "$PIECE_THIRD_SPACE─$PIECE_THIRD_SPACE─$PIECE_THIRD_SPACE"

val PRINT_TOP = "  ┌${"$HOR_LINE┬".repeat(7)}$HOR_LINE┐"
val PRINT_BET = "  ├${"$HOR_LINE┼".repeat(7)}$HOR_LINE┤"
val PRINT_BOT = "  └${"$HOR_LINE┴".repeat(7)}$HOR_LINE┘"

fun <T : ChessPiece> T.name(): String = this.javaClass.simpleName

fun String.toPosition(): Position {
    try {
        val file = Board.File.valueOf(this[0].toUpperCase().toString())
        val rank = this[1].toString().toInt()
        return Position(file, rank)
    } catch (e: Exception) {
        throw InvalidFieldException(this)
    }
}

val Color.name: String
    get() = when (this) {
        Color.WHITE -> "White"
        Color.BLACK -> "Black"
    }