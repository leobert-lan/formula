package com.instacart.formula.internal

import com.instacart.formula.FormulaContext
import com.instacart.formula.IFormula
import com.instacart.formula.BoundStream
import com.instacart.formula.Listener
import com.instacart.formula.Snapshot
import com.instacart.formula.StreamBuilder
import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext
import java.lang.IllegalStateException

internal class SnapshotImpl<out Input, State> internal constructor(
    override val input: Input,
    override val state: State,
    var transitionId: TransitionId,
    listeners: ScopedListeners,
    private val delegate: Delegate<State>,
) : FormulaContext<Input, State>(listeners), Snapshot<Input, State>, TransitionDispatcher<Input, State>, TransitionContext<Input, State> {

    override val context: FormulaContext<Input, State> = this

    var running = false
    var terminated = false

    interface Delegate<State> {
        fun <ChildInput, ChildOutput> child(
            formula: IFormula<ChildInput, ChildOutput>,
            input: ChildInput,
            transitionId: TransitionId
        ): ChildOutput

        fun handleTransitionResult(result: Transition.Result<State>)
    }

    override fun <Event> onEvent(
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return eventListener(
            key = transition.type(),
            transition = transition
        )
    }

    override fun <Event> onEvent(
        key: Any,
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return eventListener(
            key = JoinedKey(key, transition.type()),
            transition = transition
        )
    }

    override fun callback(transition: Transition<Input, State, Unit>): () -> Unit {
        val listener = onEvent(transition)
        return UnitListener(listener)
    }

    override fun callback(
        key: Any,
        transition: Transition<Input, State, Unit>,
    ): () -> Unit {
        val listener = onEvent(key, transition)
        return UnitListener(listener)
    }

    override fun <Event> eventCallback(
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return onEvent(transition)
    }

    override fun <Event> eventCallback(
        key: Any,
        transition: Transition<Input, State, Event>,
    ): Listener<Event> {
        return onEvent(key, transition)
    }

    override fun updates(init: StreamBuilder<Input, State>.() -> Unit): List<BoundStream<*>> {
        ensureNotRunning()
        val builder = StreamBuilderImpl(this)
        builder.init()
        return builder.boundedStreams
    }

    override fun <ChildOutput> child(
        child: IFormula<Unit, ChildOutput>
    ): ChildOutput {
        return child(child, Unit)
    }

    override fun <ChildInput, ChildOutput> child(
        formula: IFormula<ChildInput, ChildOutput>,
        input: ChildInput
    ): ChildOutput {
        ensureNotRunning()
        return delegate.child(formula, input, transitionId)
    }

    override fun <Event> eventListener(
        key: Any,
        transition: Transition<Input, State, Event>
    ): Listener<Event> {
        val listener = listeners.initOrFindListener<Input, State, Event>(key)
        listener.transitionDispatcher = this
        listener.transition = transition
        return listener
    }

    override fun <Event> dispatch(
        transition: Transition<Input, State, Event>,
        event: Event
    ) {
        val result = transition.toResult(this, event)
        dispatch(result)
    }

    private fun dispatch(transition: Transition.Result<State>) {
        if (!running) {
            throw IllegalStateException("Transitions are not allowed during evaluation")
        }

        if (TransitionUtils.isEmpty(transition)) {
            return
        }

        if (!terminated && transitionId.hasTransitioned()) {
            // We have already transitioned, this should not happen.
            throw IllegalStateException("Transition already happened. This is using old event listener: $transition.")
        }

        delegate.handleTransitionResult(transition)
    }

    private fun ensureNotRunning() {
        if (running) {
            throw IllegalStateException("Cannot call this transition after evaluation finished. See https://instacart.github.io/formula/faq/#after-evaluation-finished")
        }
    }
}
