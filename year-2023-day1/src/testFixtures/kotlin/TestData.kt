package io.github.hfhbd.adventofcode.year2023.day1

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object TestData {
    @OptIn(ExperimentalContracts::class)
    fun <T> use(action: Sequence<String>.() -> T): T {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        val stream = TestData::class.java.getResource("/data")!!.openStream().bufferedReader()
        return stream.useLines(action)
    }
}
