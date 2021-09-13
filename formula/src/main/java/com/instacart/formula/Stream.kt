package com.instacart.formula

/**
 * A [Stream] defines an asynchronous event(s).
 *
 * To use it within a [Formula]:
 * ```
 * Evaluation(
 *   updates = context.updates {
 *     events(stream) {
 *       transition()
 *     }
 *   }
 * )
 * ```
 *
 * @param Message A type of messages that the stream produces.
 */
typealias Stream<Message> = DisposableAction<Message>

/**
 * Emits a message as soon as [DisposableAction] is initialized.
 */
internal class StartMessageStream<Data>(
    private val data: Data
) : DisposableAction<Data> {

    override fun start(send: (Data) -> Unit): Cancelable? {
        send(data)
        return null
    }

    override fun key(): Any? = data
}

/**
 * Emits a message when [Formula] is terminated.
 */
internal object TerminateMessageStream : DisposableAction<Unit> {
    override fun start(send: (Unit) -> Unit): Cancelable {
        return Cancelable {
            send(Unit)
        }
    }

    override fun key(): Any = Unit
}