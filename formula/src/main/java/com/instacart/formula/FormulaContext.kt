package com.instacart.formula

import com.instacart.formula.internal.ScopedListeners

/**
 * Provides functionality within [evaluate][Formula.evaluate] function to [compose][child]
 * child formulas, handle events [FormulaContext.onEvent], and [respond][FormulaContext.updates]
 * to arbitrary asynchronous events.
 */
abstract class FormulaContext<out Input, State> internal constructor(
    @PublishedApi internal val listeners: ScopedListeners,
) {

    /**
     * Creates a [Listener] that takes an [Event] and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    abstract fun <Event> onEvent(
        transition: Transition<Input, State, Event>,
    ): Listener<Event>

    /**
     * Creates a [Listener] that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    abstract fun <Event> onEvent(
        key: Any,
        transition: Transition<Input, State, Event>,
    ): Listener<Event>

    /**
     * Creates a listener that takes an event and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    abstract fun callback(transition: Transition<Input, State, Unit>): () -> Unit

    /**
     * Creates a listener that takes an event and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    abstract fun callback(
        key: Any,
        transition: Transition<Input, State, Unit>,
    ): () -> Unit

    /**
     * Creates a listener that takes a [Event] and performs a [Transition].
     *
     * It uses [transition] type as key.
     */
    abstract fun <Event> eventCallback(
        transition: Transition<Input, State, Event>,
    ): Listener<Event>

    /**
     * Creates a listener that takes a [Event] and performs a [Transition].
     *
     * @param key key with which the listener is to be associated. Same key cannot be used for multiple listeners.
     */
    abstract fun <Event> eventCallback(
        key: Any,
        transition: Transition<Input, State, Event>,
    ): Listener<Event>

    /**
     * A convenience method to run a formula that takes no input. Returns the latest output
     * of the [child] formula. Formula runtime ensures the [child] is running, manages
     * its internal state and will trigger `evaluate` if needed.
     */
    abstract fun <ChildOutput> child(
        child: IFormula<Unit, ChildOutput>
    ): ChildOutput

    /**
     * Returns the latest output of the [child] formula. Formula runtime ensures the [child]
     * is running, manages its internal state and will trigger `evaluate` if needed.
     */
    abstract fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): ChildOutput

    /**
     * Provides an [StreamBuilder] that enables [Formula] to declare various events and effects.
     */
    abstract fun updates(init: StreamBuilder<Input, State>.() -> Unit): List<BoundStream<*>>

    /**
     * Scopes [create] block with a [key].
     *
     * @param key Unique identifier that will be used for this block.
     */
    inline fun <Value> key(key: Any, create: () -> Value): Value {
        listeners.enterScope(key)
        val value = create()
        listeners.endScope()
        return value
    }

    internal abstract fun <Event> eventListener(
        key: Any,
        transition: Transition<Input, State, Event>
    ): Listener<Event>
}
