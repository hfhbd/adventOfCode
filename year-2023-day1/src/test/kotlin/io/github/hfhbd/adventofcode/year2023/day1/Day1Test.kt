package io.github.hfhbd.adventofcode.year2023.day1

import kotlin.test.Test
import kotlin.test.assertEquals

class Day1Test {
    @Test
    fun partOneSample() {
        val input = sequenceOf(
            "1abc2",
            "pqr3stu8vwx",
            "a1b2c3d4e5f",
            "treb7uchet",
        )
        assertEquals(142, input.getDigits().sum())
    }

    @Test
    fun partOne() {
        val digits = TestData.use {
            getDigits().sum()
        }
        assertEquals(54450, digits)
    }

    @Test
    fun partTwoSample() {
        val input = sequenceOf(
            "two1nine",
            "eightwothree",
            "abcone2threexyz",
            "xtwone3four",
            "4nineeightseven2",
            "zoneight234",
            "7pqrstsixteen",
        )
        assertEquals(281, input.getDigits2().sum())
    }

    @Test
    fun partTwo() {
        val digits = TestData.use {
            getDigits2().sum()
        }
        assertEquals(54265, digits)
    }
}
