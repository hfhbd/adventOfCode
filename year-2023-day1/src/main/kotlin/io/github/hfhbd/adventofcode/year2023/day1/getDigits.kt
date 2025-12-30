package io.github.hfhbd.adventofcode.year2023.day1

/**
 * Return the digits of a line
 *
 * @sample io.github.hfhbd.adventofcode.year2023.day1.Day1Test.partOneSample
 * @sample io.github.hfhbd.adventofcode.year2023.day1.Day1Test.partOne
 */
fun Sequence<String>.getDigits() = map { line ->
    val digits = line.mapNotNull {
        it.digitToIntOrNull()
    }
    digits.first() * 10 + digits.last()
}
