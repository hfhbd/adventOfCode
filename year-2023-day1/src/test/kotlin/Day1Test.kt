package io.github.hfhbd.adventofcode.year2023.day1

import kotlin.test.Test
import kotlin.test.assertEquals

class Day1Test {
    @Test
    fun partOneSample() {
        val input = """
            1abc2
            pqr3stu8vwx
            a1b2c3d4e5f
            treb7uchet
        """.trimIndent()
        assertEquals(142, getDigits(input).sum())
    }

    @Test
    fun partOne() {
        assertEquals(54450, getDigits(data).sum())
    }

    @Test
    fun partTwoSample() {
        val input = """
            two1nine
            eightwothree
            abcone2threexyz
            xtwone3four
            4nineeightseven2
            zoneight234
            7pqrstsixteen
        """.trimIndent()
        assertEquals(281, getDigits2(input).sum())
    }

    @Test
    fun partTwo() {
        assertEquals(54265, getDigits2(data).sum())
    }
}
