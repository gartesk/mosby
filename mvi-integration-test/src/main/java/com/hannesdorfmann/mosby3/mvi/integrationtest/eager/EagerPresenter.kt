package com.hannesdorfmann.mosby3.mvi.integrationtest.eager

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EagerPresenter : MviBasePresenter<EagerView, String>() {

	override fun bindIntents() {
		val intent1 = intent { it.intent1() }
			.concatMap { s ->
				Observable.fromCallable {
					Thread.sleep(300)
					"$s - Result 1"
				}.subscribeOn(Schedulers.io())
			}
		val intent2 = intent { it.intent2() }
			.flatMap { s ->
				Observable.just("$s - Result 2")
					.subscribeOn(Schedulers.io())
			}
		val data = Observable.concat(intent1, intent2)
			.observeOn(AndroidSchedulers.mainThread())

		subscribeViewState(data) { view, viewState ->
			view.render(viewState)
		}
	}
}