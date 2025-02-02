package com.instacart.formula

/**
 * Converts [Action] into a [Formula] which emits [initial value][initialValue]
 * until [action][action] produces a value. It will recreate and resubscribe to
 * the [Action] whenever [Input] changes.
 */
abstract class ActionFormula<Input : Any, Output : Any> : IFormula<Input, Output> {

    /**
     * Initial value returned by this formula.
     */
    abstract fun initialValue(input: Input): Output

    /**
     * A factory function that takes an [Input] and constructs a [Action] of type [Output].
     */
    abstract fun action(input: Input): Action<Output>

    // Implements the common API used by the runtime.
    private val implementation = object : Formula<Input, Output, Output>() {
        override fun initialState(input: Input) = initialValue(input)

        override fun Snapshot<Input, Output>.evaluate(): Evaluation<Output> {
            return Evaluation(
                output = state,
                actions = context.actions {
                    action(input).onEvent {
                        transition(it)
                    }
                }
            )
        }

        override fun key(input: Input): Any = input
    }

    final override fun implementation(): Formula<Input, *, Output> = implementation
}
