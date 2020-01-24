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

import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.*
import org.junit.Test

class MviBasePresenterTest {

	@Test
	fun bindIntentsAndUnbindIntentsOnlyOnce() {

		val bindInvocations = AtomicInteger(0)
		val unbindInvocations = AtomicInteger(0)

		val view = object : MviView {}

		val presenter = object : MviBasePresenter<MviView, Any>() {
			override fun bindIntents() {
				bindInvocations.incrementAndGet()
			}

			override fun unbindIntents() {
				super.unbindIntents()
				unbindInvocations.incrementAndGet()
			}
		}

		presenter.attachView(view)
		presenter.detachView()
		presenter.attachView(view)
		presenter.detachView()
		presenter.attachView(view)
		presenter.detachView()
		presenter.destroy()

		assertEquals(1, bindInvocations.get().toLong())
		assertEquals(1, unbindInvocations.get().toLong())
	}

	@Test
	fun keepUnderlyingSubscriptions() {
		val intentsData = mutableListOf<String>()
		val businessLogic = PublishSubject.create<String>()
		val view =
			KeepUnderlyingSubscriptionsView()
		val presenter = object : MviBasePresenter<KeepUnderlyingSubscriptionsView, String>() {
			override fun bindIntents() {
				intent { it.intent }
					.subscribe { s -> intentsData.add(s) }

				subscribeViewState(businessLogic) { view, viewState ->
					view.render(viewState)
				}
			}
		}

		view.intent.onNext("Should never hit the presenter because View not attached")
		assertTrue(intentsData.isEmpty())

		presenter.attachView(view)
		view.intent.onNext("1 Intent")
		assertEquals(listOf("1 Intent"), intentsData)

		businessLogic.onNext("1 bl")
		assertEquals(listOf("1 bl"), view.renderedModels)

		businessLogic.onNext("2 bl")
		assertEquals(listOf("1 bl", "2 bl"), view.renderedModels)

		view.intent.onNext("2 Intent")
		assertEquals(listOf("1 Intent", "2 Intent"), intentsData)

		// Detach View temporarily
		presenter.detachView()
		assertFalse(view.intent.hasObservers())

		businessLogic.onNext("3 bl")
		assertEquals(listOf("1 bl", "2 bl"), view.renderedModels)

		businessLogic.onNext("4 bl")
		assertEquals(listOf("1 bl", "2 bl"), view.renderedModels)

		view.intent.onNext("Doesn't hit presenter because view not attached to presenter")
		assertEquals(listOf("1 Intent", "2 Intent"), intentsData)

		// Reattach View
		presenter.attachView(view) // This will call view.render() method
		assertEquals(listOf("1 bl", "2 bl", "4 bl"), view.renderedModels)

		view.intent.onNext("3 Intent")
		assertEquals(listOf("1 Intent", "2 Intent", "3 Intent"), intentsData)

		businessLogic.onNext("5 bl")
		assertEquals(listOf("1 bl", "2 bl", "4 bl", "5 bl"), view.renderedModels)

		// Detach View permanently
		presenter.detachView()
		presenter.destroy()
		assertFalse(businessLogic.hasObservers())
		assertFalse(view.intent.hasObservers())

		view.intent.onNext("This will never be delivered to presenter")
		assertEquals(listOf("1 Intent", "2 Intent", "3 Intent"), intentsData)

		businessLogic.onNext("This will never reach the view")
		assertEquals(listOf("1 bl", "2 bl", "4 bl", "5 bl"), view.renderedModels)
	}

	@Test
	fun resetOnViewDetachedPermanently() {
		val bindInvocations = AtomicInteger(0)
		val unbindInvocations = AtomicInteger(0)

		val view = object : MviView {}

		val presenter = object : MviBasePresenter<MviView, Any>() {
			override fun bindIntents() {
				bindInvocations.incrementAndGet()
			}

			override fun unbindIntents() {
				super.unbindIntents()
				unbindInvocations.incrementAndGet()
			}
		}

		presenter.attachView(view)
		presenter.detachView()
		presenter.destroy()
		presenter.attachView(view)
		presenter.detachView()
		presenter.attachView(view)
		presenter.detachView()
		presenter.destroy()

		assertEquals(2, bindInvocations.get().toLong())
		assertEquals(2, unbindInvocations.get().toLong())
	}

	private class KeepUnderlyingSubscriptionsView :
		MviView {

		internal var renderedModels: MutableList<String> = ArrayList()

		internal var intent = PublishSubject.create<String>()

		fun render(model: String) {
			renderedModels.add(model)
		}
	}
}
