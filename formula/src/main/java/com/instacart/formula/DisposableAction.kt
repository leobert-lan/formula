package com.instacart.formula

/**
 * An action returned by [evaluation][Formula.evaluate] that will run for any new unique
 * value of [key] and will be be cleaned up if [key] changes or if [DisposableAction] is
 * left out of evaluation.
 *
 * Disposable action can produce and send [messages][Message] back the [Formula] instance. It
 * can be used to subscribe to RxJava observables, Kotlin Flows, event bus, or any other event
 * mechanism.
 *
 * A [DisposableAction]'s key is a value that defines the identity of the [DisposableAction]. If
 * a key changes, the [DisposableAction] will dispose old instance and run again from a fresh state.
 * We also include the code call-site as a key parameter (concept called positional memoization).
 *
 * To construct a RxJava based disposable action (using formula-rxjava3 library), you can do
 * the following:
 *
 * ```kotlin
 * val action = RxDisposableAction.fromObservable { Observable.just(1, 2, 3) }
 * ```
 *
 * To use it within a [Formula]:
 * ```
 * Evaluation(
 *   updates = context.updates {
 *     action.onEvent { event ->
 *       transition()
 *     }
 *   }
 * )
 * ``
 *
 * @param Message A type of event message used to notify [Formula].
 */
interface DisposableAction<Message> {
    companion object {

        /**
         * Emits a message when [DisposableAction] is initialized. Use this stream to send effects when [Formula]
         * is initialized.
         * ```
         * events(DisposableAction.onInit()) {
         *   transition { analytics.trackViewEvent() }
         * }
         */
        fun onInit(): DisposableAction<Unit> {
            @Suppress("UNCHECKED_CAST")
            return StartMessageStream(Unit)
        }

        /**
         * Emits a message with [data] when [DisposableAction] is initialized. Uses [data] as key.
         *
         * Use this stream to send a effects with latest [Data] value.
         * ```
         * events(Stream.onData(itemId)) {
         *   transition { api.fetchItem(itemId) }
         * }
         * ```
         */
        fun <Data> onData(data: Data): DisposableAction<Data> {
            return StartMessageStream(data)
        }

        /**
         * Emits a message when [Formula] is terminated.
         * ```
         * events(Stream.onTerminate()) {
         *   transition { analytics.trackCloseEvent() }
         * }
         * ```
         *
         * Note that transitions to new state will be discarded because [Formula] is terminated. This is best to
         * use to notify other services/analytics of [Formula] termination.
         */
        fun onTerminate(): DisposableAction<Unit> {
            return TerminateMessageStream
        }
    }

    /**
     * This method is called when [DisposableAction] is first returned from [evaluation][Formula.evaluate].
     *
     * @param send Use this callback to pass messages back to [Formula].
     *             Note: you need to call this on the main thread.
     */
    fun start(send: (Message) -> Unit): Cancelable?

    /**
     * Used to distinguish between different types of Streams.
     */
    fun key(): Any?
}