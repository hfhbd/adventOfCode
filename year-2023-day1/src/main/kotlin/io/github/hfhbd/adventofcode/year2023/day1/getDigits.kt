package io.github.hfhbd.adventofcode.year2023.day1

fun Sequence<String>.getDigits() = map { line ->
    val digits = line.mapNotNull {
        it.digitToIntOrNull()
    }
    digits.first() * 10 + digits.last()
}
