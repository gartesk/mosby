package com.hannesdorfmann.mosby3.mvi

import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject

/**
 * Just a simple [DisposableObserver] that is used to cancel subscriptions from view's
 * state to the internal relays
 */
internal class DisposableViewStateObserver<VS>(private val subject: BehaviorSubject<VS>) :
	DisposableObserver<VS>() {

	override fun onNext(value: VS) {
		subject.onNext(value)
	}

	override fun onError(e: Throwable) {
		throw IllegalStateException("ViewState observable must not reach error state - onError()", e)
	}

	override fun onComplete() {
		// ViewState observable never completes so ignore any complete event
	}
}
