package com.hannesdorfmann.mosby3.mvi

import com.hannesdorfmann.mosby3.mvp.MvpView

import org.junit.Test

import java.util.ArrayList

import io.reactivex.Observable
import org.junit.Assert.assertEquals

class EagerViewStartWithTest {


	private class EagerViewStartWith : MvpView {

		internal var renderedStates: MutableList<String> = ArrayList()

		fun intent1(): Observable<String> {
			return Observable.just("Intent 1").startWith("Before Intent 1")
		}

		fun intent2(): Observable<String> {
			return Observable.just("Intent 2")
		}

		fun render(state: String) {
			renderedStates.add(state)
		}
	}

	private class EagerPresenter : MviBasePresenter<EagerViewStartWith, String>() {
		override fun bindIntents() {
			val intent1 = intent { it.intent1() }
			val intent2 = intent { it.intent2() }

			val res1 = intent1.flatMap { s -> Observable.just("$s - Result 1") }
			val res2 = intent2.flatMap { s -> Observable.just("$s - Result 2") }

			val merged = Observable.merge(res1, res2)

			subscribeViewState(merged) { view, viewState ->
				view.render(viewState)
			}
		}
	}


	@Test
	fun viewWithStartWithIntentWorksProperly() {
		val view = EagerViewStartWith()
		val presenter = EagerPresenter()
		presenter.attachView(view)

		assertEquals(
			listOf("Before Intent 1 - Result 1", "Intent 1 - Result 1", "Intent 2 - Result 2"),
			view.renderedStates
		)

	}

}
