package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.activity

import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestView
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject

class MviFinishInOnStartActivity :
	MviActivity<LifecycleTestView, LifecycleTestPresenter>(), LifecycleTestView {

	var onDestroyReached: Subject<Unit> = ReplaySubject.create()

	override fun createPresenter(): LifecycleTestPresenter {
		return LifecycleTestPresenter()
	}

	override fun onStart() {
		super.onStart()
		finish() // finish  --> this is what we would like to test --> App should not crash
	}

	override fun onDestroy() {
		super.onDestroy()
		onDestroyReached.onNext(Unit)
		onDestroyReached.onComplete()
	}
}