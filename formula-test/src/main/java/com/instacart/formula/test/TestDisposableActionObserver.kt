package com.instacart.formula.test

import com.instacart.formula.Cancelable
import com.instacart.formula.DisposableAction
import java.lang.AssertionError

class TestDisposableActionObserver<Message>(private val action: DisposableAction<Message>) {
    private val values = mutableListOf<Message>()
    private val cancelation = action.start { values.add(it) }

    fun values(): List<Message> = values

    fun assertValues(vararg expected: Message) {
        if (expected.size != values.size) {
            throw AssertionError("Value count differs; expected: ${expected.size}, was: ${values.size}")
        }

        expected.zip(values).forEachIndexed { index, (expected, value) ->
            if (expected != value) {
                throw AssertionError("Values are different at $index; expected: $expected, was: $value")
            }
        }
    }

    /**
     * Attempts to cancel the [action]. Will throw an exception if [action] did not
     * provide a [Cancelable].
     */
    fun cancel() {
        cancelation!!.cancel()
    }
}
