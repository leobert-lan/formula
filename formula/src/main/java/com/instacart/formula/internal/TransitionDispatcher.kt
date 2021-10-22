package com.instacart.formula.internal

import com.instacart.formula.Transition
import com.instacart.formula.TransitionContext

internal interface TransitionDispatcher<out Input, State> {
    fun <Event> dispatch(transition: Transition<Input, State, Event>, event: Event)
}

internal fun <Input, State, Event> Transition<Input, State, Event>.toResult(
    context: TransitionContext<Input, State>,
    event: Event
): Transition.Result<State> {
    return context.run { toResult(event) }
}