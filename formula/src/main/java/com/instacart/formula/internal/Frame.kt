package com.instacart.formula.internal

import com.instacart.formula.Evaluation

/**
 * A frame is a representation of state after a process round. After each processing round
 * we need to look at what children and updates exist and do a diff.
 */
internal class Frame<Input, State, Output>(
    val context: SnapshotImpl<Input, State>,
    val evaluation: Evaluation<Output>,
) {

    private var stateValid: Boolean = true
    private var childrenValid: Boolean = true

    val input: Input = context.input
    val state: State = context.state

    fun updateStateValidity(state: State) {
        if (stateValid && this.state != state) {
            stateValid = false
        }
    }

    fun childInvalidated() {
        childrenValid = false
    }

    fun isValid() = stateValid && childrenValid

    fun isValid(input: Input): Boolean {
        return stateValid && childrenValid && this.input == input
    }
}
