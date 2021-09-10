package com.instacart.formula.rxjava3

import com.instacart.formula.Cancelable
import com.instacart.formula.DisposableAction
import io.reactivex.rxjava3.core.Observable

/**
 * Adapter to convert RxJava3 observables to [DisposableAction].
 */
interface RxDisposableAction<Message> : DisposableAction<Message> {
    companion object {
        /**
         * Creates a [DisposableAction] from an [Observable] factory [create].
         *
         * ```
         * events(RxStream.fromObservable { locationManager.updates() }) { event ->
         *   transition()
         * }
         * ```
         */
        inline fun <Message> fromObservable(
            crossinline create: () -> Observable<Message>
        ): DisposableAction<Message> {
            return object : RxDisposableAction<Message> {

                override fun observable(): Observable<Message> {
                    return create()
                }

                override fun key(): Any = Unit
            }
        }

        /**
         * Creates a [DisposableAction] from an [Observable] factory [create].
         *
         * ```
         * events(RxStream.fromObservable(itemId) { repo.fetchItem(itemId) }) { event ->
         *   transition()
         * }
         * ```
         *
         * @param key Used to distinguish this [DisposableAction] from other actions.
         */
        inline fun <Message> fromObservable(
            key: Any?,
            crossinline create: () -> Observable<Message>
        ): DisposableAction<Message> {
            return object : RxDisposableAction<Message> {

                override fun observable(): Observable<Message> {
                    return create()
                }

                override fun key(): Any? = key
            }
        }
    }

    fun observable(): Observable<Message>

    override fun start(send: (Message) -> Unit): Cancelable? {
        val disposable = observable().subscribe(send)
        return Cancelable(disposable::dispose)
    }
}
