package io.github.hfhbd.adventofcode.day3

fun getParts(input: String) = sequence {
    val lines = input.lines()
    for ((lineNumber, line) in lines.withIndex()) {

        var currentIndex = 0

        while (true) {
            val char = line.getOrNull(currentIndex) ?: break
            when {
                char.isDigit() -> {
                    var currentNumber = char.toString()
                    val startIndex = currentIndex

                    // consume the number
                    while (true) {
                        val nextChar = line.getOrNull(currentIndex + 1) ?: break
                        when {
                            nextChar.isDigit() -> currentNumber += nextChar
                            else -> break
                        }
                        currentIndex += 1
                    }
                    val number = currentNumber.toInt()

                    // left
                    val left = line.getOrNull(currentIndex + 1)
                    val right = line.getOrNull(startIndex - 1)
                    val upper = lines.getOrNull(lineNumber - 1)?.substring2(startIndex, currentIndex)
                    val down = lines.getOrNull(lineNumber + 1)?.substring2(startIndex, currentIndex)

                    val hasLeft = left != null && left != '.'
                    val hasRight = right != null && right != '.'
                    val hasUpper = upper != null && upper.any {
                        !(it.isDigit() || it == '.')
                    }
                    val hasDown = down != null && down.any {
                        !(it.isDigit() || it == '.')
                    }

                    if (hasLeft || hasRight || hasUpper || hasDown) {
                        yield(number)
                    }
                }
            }
            currentIndex++
        }
    }
}

private fun String.substring2(startIndex: Int, endIndex: Int): String {
    val newStartIndex = if (startIndex == 0) 0 else startIndex - 1
    val newEnd = if (endIndex == lastIndex) lastIndex else endIndex + 1
    return substring(newStartIndex..newEnd)
}
