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
package com.hannesdorfmann.mosby3.mvi.integrationtest.backstack

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.hannesdorfmann.mosby3.FragmentMviDelegateImpl.Companion.DEBUG
import com.hannesdorfmann.mosby3.mvi.integrationtest.backstack.BackstackActivity.Companion.navigateToSecondFragment
import com.hannesdorfmann.mosby3.mvi.integrationtest.backstack.BackstackActivity.Companion.pressBackButton
import com.hannesdorfmann.mosby3.mvi.integrationtest.backstack.BackstackActivity.Companion.rotateToLandscape
import com.hannesdorfmann.mosby3.mvi.integrationtest.backstack.BackstackActivity.Companion.rotateToPortrait
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class BackstackActivityTest {

	@get:Rule
	var rule = ActivityTestRule(BackstackActivity::class.java)

	@After
	fun afterActivityFinished() {
		assertEquals(3, BackstackActivity.firstPresenter.detachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.firstPresenter.unbindIntentCalls.get().toLong())
	}

	@Test
	fun testConfigChange() {
		DEBUG = true
		sleep(1000)
		assertEquals(1, BackstackActivity.createFirstPresenterCalls.get().toLong())
		assertEquals(1, BackstackActivity.firstPresenter.attachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.firstPresenter.bindIntentCalls.get().toLong())

		// Screen orientation change
		rotateToLandscape()
		sleep(1000)
		assertEquals(1, BackstackActivity.firstPresenter.detachViewCalls.get().toLong())
		assertEquals(0, BackstackActivity.firstPresenter.unbindIntentCalls.get().toLong())
		assertEquals(1, BackstackActivity.createFirstPresenterCalls.get().toLong())
		assertEquals(2, BackstackActivity.firstPresenter.attachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.firstPresenter.bindIntentCalls.get().toLong())

		// Navigate to next fragment
		sleep(1000)
		navigateToSecondFragment()
		sleep(1000)
		assertEquals(2, BackstackActivity.firstPresenter.detachViewCalls.get().toLong())
		assertEquals(0, BackstackActivity.firstPresenter.unbindIntentCalls.get().toLong())
		assertEquals(1, BackstackActivity.createFirstPresenterCalls.get().toLong())
		assertEquals(2, BackstackActivity.firstPresenter.attachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.firstPresenter.bindIntentCalls.get().toLong())

		// Check Second Fragment
		assertEquals(1, BackstackActivity.createSecondPresenterCalls.get().toLong())
		assertEquals(1, BackstackActivity.secondPresenter.attachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.secondPresenter.bindIntentCalls.get().toLong())

		// Screen orientation change
		rotateToPortrait()
		sleep(1000)
		assertEquals(1, BackstackActivity.secondPresenter.detachViewCalls.get().toLong())
		assertEquals(0, BackstackActivity.secondPresenter.unbindIntentCalls.get().toLong())
		assertEquals(1, BackstackActivity.createSecondPresenterCalls.get().toLong())
		assertEquals(2, BackstackActivity.secondPresenter.attachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.secondPresenter.bindIntentCalls.get().toLong())

		// Press back button --> Finish second fragment
		pressBackButton()
		sleep(1000)
		assertEquals(2, BackstackActivity.secondPresenter.detachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.secondPresenter.unbindIntentCalls.get().toLong())

		// First Fragment restored from backstack
		assertEquals(1, BackstackActivity.createFirstPresenterCalls.get().toLong())
		assertEquals(3, BackstackActivity.firstPresenter.attachViewCalls.get().toLong())
		assertEquals(1, BackstackActivity.firstPresenter.bindIntentCalls.get().toLong())

		// Press back button --> finishes the activity
		pressBackButton()
		sleep(2000)
	}
}