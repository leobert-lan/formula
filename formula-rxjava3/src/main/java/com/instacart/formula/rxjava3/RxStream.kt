package com.instacart.formula.rxjava3

import com.instacart.formula.DisposableAction

/**
 * Formula [DisposableAction] adapter to enable RxJava use.
 */
@Deprecated("Use RxDisposableAction")
typealias RxStream<Message> = RxDisposableAction<Message>