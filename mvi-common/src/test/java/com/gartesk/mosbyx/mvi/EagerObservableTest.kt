/*
 * Copyright 2020 MosbyX contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gartesk.mosbyx.mvi

import com.gartesk.mosbyx.mvp.MvpView
import io.reactivex.Observable
import java.util.ArrayList
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests if observables (like intents) are submitting directly in on subscribe,
 * that all events will be dispatched properly to view.render() and no one has been swallowed
 * because the observable stream hasn't been fully established yet.
 */
class EagerObservableTest {

	private class EagerView : MvpView {

		internal var renderedStates: MutableList<String> = ArrayList()

		fun intent1(): Observable<String> {
			return Observable.just("Intent 1")
		}

		fun intent2(): Observable<String> {
			return Observable.just("Intent 2")
		}

		fun render(state: String) {
			renderedStates.add(state)
		}
	}


	private class EagerPresenter : MviBasePresenter<EagerView, String>() {
		override fun bindIntents() {
			val intent1 = intent { it.intent1() }
			val intent2 = intent { it.intent2() }

			val res1 = intent1.flatMap { Observable.just("Result 1") }
			val res2 = intent2.flatMap { Observable.just("Result 2") }

			val merged = Observable.merge(res1, res2)

			subscribeViewState(merged) { view, viewState ->
				view.render(viewState)
			}
		}
	}

	@Test
	fun connectEager() {
		val view = EagerView()
		val presenter = EagerPresenter()

		presenter.attachView(view)

		assertEquals(listOf("Result 1", "Result 2"), view.renderedStates)
	}


}
