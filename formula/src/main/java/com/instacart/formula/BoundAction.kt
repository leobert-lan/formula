package com.instacart.formula

/**
 * An action combined with event listener.
 */
class BoundAction<Message>(
    val key: Any,
    val stream: Stream<Message>,
    internal var listener: (Message) -> Unit
) {

    internal var cancelable: Cancelable? = null

    internal fun start() {
        cancelable = stream.start() { message ->
            listener.invoke(message)
        }
    }

    internal fun tearDown() {
        cancelable?.cancel()
        cancelable = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoundAction<*>

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    fun keyAsString(): String {
        return key.toString()
    }
}
