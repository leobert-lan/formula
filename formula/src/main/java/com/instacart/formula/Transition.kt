package com.instacart.formula

/**
 * Defines an intent to transition by emitting a new [State] and optional [Action].
 */
sealed class Transition<out State> {
    companion object {
        /**
         * A convenience method to define transitions.
         *
         * ```
         * fun nameChanged(state: FormState, newName: String) = Transition.create {
         *   transition(state.copy(name = newName))
         * }
         * ```
         */
        inline fun <State> create(init: Factory.() -> Transition<State>): Transition<State> {
            return init(Factory)
        }
    }

    /**
     * Stateful transition.
     *
     * @param state New state
     * @param action An optional deferred action that will be executed by the Formula runtime. Within
     * this action block, you can trigger listeners, log analytics, trigger database writes,
     * trigger fire and forget network requests, etc.
     */
    data class Stateful<State>(val state: State, override val action: Action? = null) : Transition<State>()

    /**
     * Only action is emitted as part of this transition.
     *
     * @param action A deferred action that will be executed by the Formula runtime. Within
     * this action block, you can trigger listeners, log analytics, trigger database writes,
     * trigger fire and forget network requests, etc.
     */
    data class OnlyAction(override val action: Action) : Transition<Nothing>()

    /**
     * Nothing happens in this transition.
     */
    object None : Transition<Nothing>() {
        override val action: Action? = null
    }


    /**
     * Factory uses as a receiver parameter to provide transition constructor dsl.
     */
    object Factory {

        /**
         * A transition that does nothing.
         */
        fun none(): Transition<Nothing> {
            return None
        }

        /**
         * Creates a transition to a new [State] and executes [invokeAction] callback
         * after the state change.
         */
        fun <State> transition(
            state: State,
            invokeAction: (() -> Unit)? = null
        ): Stateful<State> {
            return Stateful(state, invokeAction)
        }

        /**
         * Creates a transition that only executes [invokeAction].
         */
        fun transition(
            invokeAction: () -> Unit
        ): OnlyAction {
            return OnlyAction(invokeAction)
        }
    }

    abstract val action: Action?
}