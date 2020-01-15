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
package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.viewgroup

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.hannesdorfmann.mosby3.ViewGroupMviDelegateImpl.Companion.DEBUG
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class MviViewGroupContainerActivityTest2 {

	@get:Rule
	var rule = ActivityTestRule(MviViewGroupContainerActivity::class.java)

	private lateinit var presenter: LifecycleTestPresenter

	@Test
	fun testRemoveMviViewGroupManually() {
		DEBUG = true
		val viewGroup: TestMviFrameLayout = MviViewGroupContainerActivity.mviViewGroup
		presenter = viewGroup.presenter

		assertNotNull(presenter)
		assertEquals(1, viewGroup.createPresenterInvocations)
		assertEquals(1, presenter.attachViewInvocations)
		assertEquals(1, presenter.bindIntentInvocations)
		assertTrue(presenter.attachedView === viewGroup)
		sleep(1000)

		// Screen orientation change
		MviViewGroupContainerActivity.removeMviViewGroup()
		sleep(1000)
		assertEquals(1, presenter.detachViewInvocations)
		assertEquals(1, presenter.unbindIntentInvocations)
		assertEquals(1, presenter.destroyInvocations)
		assertEquals(1, viewGroup.createPresenterInvocations)
		assertEquals(1, presenter.attachViewInvocations)
		assertNull(presenter.attachedView)
	}
}