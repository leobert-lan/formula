package com.instacart.formula.test

import com.google.common.truth.Truth.assertThat
import com.instacart.formula.Cancelable
import com.instacart.formula.DisposableAction
import org.junit.Test
import java.lang.IllegalStateException

class TestDisposableActionTest {

    @Test fun `assert values success`() {
        multipleEventAction().test().assertValues(1, 2)
    }

    @Test fun `assert value fails due to different size`() {
        val exception = fails { multipleEventAction().test().assertValues(1) }
        assertThat(exception).isInstanceOf(AssertionError::class.java)
    }

    @Test fun `assert value fails due to different value`() {
        val exception = fails { multipleEventAction().test().assertValues(1, 5) }
        assertThat(exception).isInstanceOf(AssertionError::class.java)
    }

    inline fun fails(action: () -> Unit): Throwable {
        try {
            action()
        } catch (t: Error) {
            return t
        }

        throw IllegalStateException("Action succeeded.")
    }

    private fun multipleEventAction() = object : DisposableAction<Int> {
        override fun start(send: (Int) -> Unit): Cancelable? {
            send(1)
            send(2)
            return null
        }

        override fun key(): Any = Unit
    }
}
