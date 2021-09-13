package com.instacart.formula

import com.instacart.formula.internal.JoinedKey
import com.instacart.formula.internal.ScopedCallbacks

/**
 * Provides functionality within [evaluate][Formula.evaluate] function to [compose][child]
 * child formulas, handle events [FormulaContext.onEvent], and [respond][FormulaContext.updates]
 * to arbitrary asynchronous events.
 */
abstract class FormulaContext<State> internal constructor(
    @PublishedApi internal val callbacks: ScopedCallbacks
) {

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * It uses inlined callback anonymous class for type.
     */
    inline fun onEvent(crossinline transition: Transition.Factory.() -> Transition<State>): () -> Unit {
        val callback: (Unit) -> Unit = {
            performTransition(transition(Transition.Factory))
        }
        val reference = callbacks.initOrFindCallback(callback::class)
        reference.delegate = callback
        return reference
    }

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    inline fun onEvent(
        key: Any,
        crossinline transition: Transition.Factory.() -> Transition<State>
    ): () -> Unit {
        val callback = callbacks.initOrFindCallback(key)
        callback.delegate = {
            performTransition(transition(Transition.Factory))
        }
        return callback
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * It uses inlined callback anonymous class for type.
     */
    inline fun <UIEvent> onEvent(
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State>
    ): (UIEvent) -> Unit {
        val callback: (UIEvent) -> Unit = {
            performTransition(transition(Transition.Factory, it))
        }

        val reference = callbacks.initOrFindEventCallback<UIEvent>(callback::class)
        reference.delegate = callback
        return reference
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    inline fun <UIEvent> onEvent(
        key: Any,
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State>
    ): (UIEvent) -> Unit {
        val callback = callbacks.initOrFindEventCallback<UIEvent>(key)
        callback.delegate = {
            performTransition(transition(Transition.Factory, it))
        }
        return callback
    }

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * It uses inlined callback anonymous class for type.
     */
    inline fun callback(crossinline transition: Transition.Factory.() -> Transition<State>): () -> Unit {
        return onEvent(transition)
    }

    /**
     * Creates a callback to be used for handling UI event transitions.
     *
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    inline fun callback(
        key: Any,
        crossinline transition: Transition.Factory.() -> Transition<State>
    ): () -> Unit {
        return onEvent(key, transition)
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * It uses inlined callback anonymous class for type.
     */
    inline fun <UIEvent> eventCallback(
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State>
    ): (UIEvent) -> Unit {
        return onEvent(transition)
    }

    /**
     * Creates a callback that takes a [UIEvent] and performs a [Transition].
     *
     * @param key key with which the callback is to be associated. Same key cannot be used for multiple callbacks.
     */
    inline fun <UIEvent> eventCallback(
        key: Any,
        crossinline transition: Transition.Factory.(UIEvent) -> Transition<State>
    ): (UIEvent) -> Unit {
        return onEvent(key, transition)
    }

    /**
     * A convenience method to run a formula that takes no input. Returns the latest output
     * of the [child] formula. Formula runtime ensures the [child] is running, manages
     * its internal state and will trigger `evaluate` if needed.
     */
    fun <ChildOutput> child(
        child: IFormula<Unit, ChildOutput>
    ): ChildOutput {
        return child(child, Unit)
    }

    /**
     * Returns the latest output of the [child] formula. Formula runtime ensures the [child]
     * is running, manages its internal state and will trigger `evaluate` if needed.
     */
    abstract fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): ChildOutput

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    fun updates(init: UpdateBuilder<State>.() -> Unit): List<BoundAction<*>> = actions(init)

    /**
     * Provides an [UpdateBuilder] that enables [Formula] to declare various events and effects.
     */
    abstract fun actions(init: UpdateBuilder<State>.() -> Unit): List<BoundAction<*>>

    /**
     * Scopes [create] block with a [key].
     *
     * @param key Unique identifier that will be used for this block.
     */
    inline fun <Value> key(key: Any, create: () -> Value): Value {
        callbacks.enterScope(key)
        val value = create()
        callbacks.endScope()
        return value
    }

    @PublishedApi internal abstract fun performTransition(transition: Transition<State>)

    /**
     * Provides methods to declare various events and effects.
     */
    class UpdateBuilder<State>(
        @PublishedApi internal val transitionCallback: (Transition<State>) -> Unit
    ) {
        internal val actions = mutableListOf<BoundAction<*>>()

        /**
         * Adds a [DisposableAction] as part of this [Evaluation]. [DisposableAction] will be
         * subscribed when it is initially added and unsubscribed when it is not
         * returned as part of [Evaluation].
         *
         * @param transition Callback invoked when [DisposableAction] sends us a [Message].
         */
        inline fun <Message> events(
            action: DisposableAction<Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            add(toBoundAction(action, transition))
        }

        /**
         * Adds a [DisposableAction] as part of this [Evaluation]. [DisposableAction] will be
         * subscribed when it is initially added and unsubscribed when it is not returned
         * as part of [Evaluation].
         *
         * @param transition Callback invoked when [DisposableAction] sends us a [Message].
         */
        inline fun <Message> onEvent(
            action: DisposableAction<Message>,
            avoidParameterClash: Any = this,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            add(toBoundAction(action, transition))
        }

        /**
         * Adds a [DisposableAction] as part of this [Evaluation]. [DisposableAction] will be
         * subscribed when it is initially added and unsubscribed when it is not returned
         * as part of [Evaluation].
         *
         * @param transition Callback invoked when [DisposableAction] sends us a [Message].
         *
         * Example:
         * ```
         * DisposableAction.onInit().onEvent {
         *   transition { /* */ }
         * }
         * ```
         */
        inline fun <Message> DisposableAction<Message>.onEvent(
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ) {
            val stream = this
            this@UpdateBuilder.events(stream, transition)
        }

        @PublishedApi internal fun add(action: BoundAction<*>) {
            if (actions.contains(action)) {
                throw IllegalStateException("duplicate stream with key: ${action.keyAsString()}")
            }

            actions.add(action)
        }

        @PublishedApi internal inline fun <Message> toBoundAction(
            action: DisposableAction<Message>,
            crossinline transition: Transition.Factory.(Message) -> Transition<State>
        ): BoundAction<Message> {
            val callback: (Message) -> Unit = {
                val value = transition(Transition.Factory, it)
                transitionCallback(value)
            }

            return BoundAction(
                key = JoinedKey(action.key(), callback::class),
                stream = action,
                listener = callback
            )
        }
    }
}