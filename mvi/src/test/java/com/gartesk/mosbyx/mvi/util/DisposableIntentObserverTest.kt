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
package com.gartesk.mosbyx.mvi.util

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class DisposableIntentObserverTest {

	@Test
	fun forwardOnNextAndOnCompleteToPublishSubject() {
		val subject = PublishSubject.create<String>()

		val sub = TestObserver<String>()
		subject.subscribeWith(sub)

		Observable.just("Hello").subscribe(
			DisposableIntentObserver(
				subject
			)
		)

		sub.assertNoErrors().assertComplete().assertResult("Hello")
	}

	@Test
	fun error() {
		val subject = PublishSubject.create<Any>()
		val subscriber = TestObserver<Any>()
		subject.subscribeWith(subscriber)

		val originalException = RuntimeException("I am the original Exception")
		val expectedException = IllegalStateException("View intents must not throw errors", originalException)
		try {
			Observable.error<Any>(originalException).subscribe(
				DisposableIntentObserver(
					subject
				)
			)
			fail("Exception expected")
		} catch (e: Throwable) {
			val cause = e.cause!!
			assertEquals(expectedException.message, cause.message)
			assertEquals(originalException, cause.cause)
		}

		subscriber.assertNotComplete().assertNoValues()
	}
}
