import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationTest {
    @Test
    fun foo() {
        assertEquals("bar", System.getenv("foo"))
    }
}
