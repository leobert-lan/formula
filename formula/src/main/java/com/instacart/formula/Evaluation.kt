package com.instacart.formula

/**
 * The result of [evaluate][Formula.evaluate] function.
 *
 * @param Output Usually a data class returned by formula that contains data and callbacks.
 * When it is used to render UI, we call it a render model (Ex: ItemRenderModel).
 *
 * @param actions A list of actions that will be performed by formula runtime.
 */
data class Evaluation<out Output>(
    val output: Output,
    val actions: List<BoundAction<*>> = emptyList()
) {

    companion object {
        @Deprecated("Replace `updates` with `actions`.")
        operator fun <Output> invoke(output: Output, updates: List<BoundAction<*>>): Evaluation<Output> {
            return Evaluation(
                output = output,
                actions = updates
            )
        }
    }
}
