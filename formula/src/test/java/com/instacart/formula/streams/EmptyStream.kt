package com.instacart.formula.streams

import com.instacart.formula.rxjava3.RxDisposableAction
import com.instacart.formula.rxjava3.RxStream
import io.reactivex.rxjava3.core.Observable

object EmptyStream {
    fun init(key: Any = Unit) = RxDisposableAction.fromObservable(key) { Observable.empty<Unit>() }
}
