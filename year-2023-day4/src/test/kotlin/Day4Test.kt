import day4.`WO-RLD`
import day4.day4
import kotlin.test.Test
import kotlin.test.assertEquals

class Day4Test {
    @Test
    fun partOne() {
        assertEquals("WORLD!", `WO-RLD`)
        day4()
        assertEquals("42", `WO-RLD`)
    }
}
