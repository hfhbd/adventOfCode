package io.github.hfhbd.adventofcode.day1

fun getDigits2(input: String) = input.lineSequence().map {
    // fast-path
    if (it.first().isDigit() && it.last().isDigit()) {
        return@map (it.first().digitToInt() * 10) + it.last().digitToInt()
    }

    var first: Int? = null
    var last: Int? = null

    for ((index, char) in it.withIndex()) {
        val newFirst = when {
            char.isDigit() -> char.digitToInt()
            it.contains(index, "one") -> 1
            it.contains(index, "two") -> 2
            it.contains(index, "three") -> 3
            it.contains(index, "four") -> 4
            it.contains(index, "five") -> 5
            it.contains(index, "six") -> 6
            it.contains(index, "seven") -> 7
            it.contains(index, "eight") -> 8
            it.contains(index, "nine") -> 9
            else -> continue
        }
        first = newFirst
        break
    }

    for (index in it.lastIndex downTo 0) {
        val char = it[index]
        val newLast = when {
            char.isDigit() -> char.digitToInt()
            it.containsReverse(index, "one") -> 1
            it.containsReverse(index, "two") -> 2
            it.containsReverse(index, "three") -> 3
            it.containsReverse(index, "four") -> 4
            it.containsReverse(index, "five") -> 5
            it.containsReverse(index, "six") -> 6
            it.containsReverse(index, "seven") -> 7
            it.containsReverse(index, "eight") -> 8
            it.containsReverse(index, "nine") -> 9
            else -> continue
        }

        last = newLast
        break
    }

    return@map (first!! * 10) + last!!
}

private fun String.containsReverse(index: Int, content: String) = contains(index - content.lastIndex, content)
private fun String.contains(index: Int, content: String): Boolean {
    if (index < 0) {
        return false
    }
    if (index + content.length - 1 > lastIndex) {
        return false
    }
    return subSequence(index..<index + content.length) == content
}
