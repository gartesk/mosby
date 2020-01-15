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
package com.gartesk.mosbyx.mvi.integrationtest.lifecycle.activity

import android.content.pm.ActivityInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.gartesk.mosbyx.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class MviLifecycleActivityTest {

	@get:Rule
	var rule = ActivityTestRule(MviLifecycleActivity::class.java)

	private lateinit var presenter: LifecycleTestPresenter

	@After
	fun checkPresenterNotRetained() {
		assertNotNull(presenter)
		assertEquals(2, presenter.detachViewInvocations)
		assertEquals(1, presenter.unbindIntentInvocations)
		assertEquals(1, presenter.destroyInvocations)
	}

	@Test
	fun testConfigChange() {
		val portraitActivity = rule.activity
		presenter = portraitActivity.presenter
		assertNotNull(presenter)
		assertEquals(1, MviLifecycleActivity.createPresenterInvocations)
		assertEquals(1, presenter.attachViewInvocations)
		assertTrue(presenter.attachedView === portraitActivity)
		assertEquals(1, presenter.bindIntentInvocations)
		sleep(1000)

		// Screen orientation change
		portraitActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		sleep(1000)
		assertEquals(1, presenter.detachViewInvocations)
		assertEquals(1, presenter.bindIntentInvocations)
		assertEquals(0, presenter.unbindIntentInvocations)
		assertEquals(0, presenter.destroyInvocations)
		assertEquals(1, MviLifecycleActivity.createPresenterInvocations)
		assertEquals(2, presenter.attachViewInvocations)
		assertTrue(presenter.attachedView !== portraitActivity && presenter.attachedView != null)

		// Press back button --> Finishes Activity
		MviLifecycleActivity.pressBackButton()
		sleep(1000)
	}
}