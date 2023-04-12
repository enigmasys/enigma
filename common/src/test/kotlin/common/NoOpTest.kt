package common

import org.junit.jupiter.api.Test

class NoOpTest {
    @Test
    fun noop() {
        assert(true) { "This test fails" }
    }
}