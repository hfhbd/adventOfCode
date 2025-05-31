package io.github.hfhbd.adventofcode.year2023.day1

fun getDigits(input: String) = input.lineSequence().map {
    val digits = it.mapNotNull {
        it.digitToIntOrNull()
    }
    (digits.first() * 10) + digits.last()
}
