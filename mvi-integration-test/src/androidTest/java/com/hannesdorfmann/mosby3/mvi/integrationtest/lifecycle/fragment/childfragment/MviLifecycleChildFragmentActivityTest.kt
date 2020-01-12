/*
 * Copyright 2016 Hannes Dorfmann.
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
 *
 */
package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.fragment.childfragment

import android.content.pm.ActivityInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class MviLifecycleChildFragmentActivityTest {

	@get:Rule
	var rule = ActivityTestRule(MviLifecycleChildFragmentActivity::class.java)

	private lateinit var activityPresenter: LifecycleTestPresenter
	private lateinit var fragmentPresenter: LifecycleTestPresenter
	private lateinit var childFragmentPresenter: LifecycleTestPresenter

	@After
	fun checkPresenterNotRetained() {
		assertNotNull(activityPresenter)
		assertEquals(2, activityPresenter.detachViewInvocations)
		assertEquals(1, activityPresenter.unbindIntentInvocations)
		assertEquals(1, activityPresenter.destroyInvocations)
		assertNotNull(fragmentPresenter)
		assertEquals(2, fragmentPresenter.detachViewInvocations)
		assertEquals(1, fragmentPresenter.unbindIntentInvocations)
		assertEquals(1, fragmentPresenter.destroyInvocations)
		assertNotNull(childFragmentPresenter)
		assertEquals(2, childFragmentPresenter.detachViewInvocations)
		assertEquals(1, childFragmentPresenter.unbindIntentInvocations)
		assertEquals(1, childFragmentPresenter.destroyInvocations)
	}

	@Test
	fun testConfigChange() {
		val activity = rule.activity
		activityPresenter = activity.presenter

		assertNotNull(activityPresenter)
		assertEquals(1, MviLifecycleChildFragmentActivity.createPresenterInvocations)
		assertEquals(1, activityPresenter.attachViewInvocations)
		assertEquals(1, activityPresenter.bindIntentInvocations)
		assertTrue(activityPresenter.attachedView === activity)

		val fragment = activity.fragment
		fragmentPresenter = fragment.presenter
		assertNotNull(fragmentPresenter)
		assertEquals(1, ContainerMviLifecycleFragment.createPresenterInvocations)
		assertEquals(1, fragmentPresenter.attachViewInvocations)
		assertEquals(1, fragmentPresenter.bindIntentInvocations)
		assertTrue(fragmentPresenter.attachedView === fragment)

		val childFragment = fragment.childFragment
		childFragmentPresenter = fragment.presenter
		assertNotNull(childFragment)
		assertEquals(1, MviLifecycleChildFragment.createPresenterInvocations)
		assertEquals(1, childFragmentPresenter.attachViewInvocations)
		assertEquals(1, childFragmentPresenter.bindIntentInvocations)
		assertTrue(childFragmentPresenter.attachedView === fragment)
		sleep(1000)

		// Screen orientation change
		activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		sleep(1000)
		assertEquals(1, activityPresenter.detachViewInvocations)
		assertEquals(0, activityPresenter.unbindIntentInvocations)
		assertEquals(0, activityPresenter.destroyInvocations)
		assertEquals(1, MviLifecycleChildFragmentActivity.createPresenterInvocations)
		assertEquals(1, activityPresenter.bindIntentInvocations)
		assertEquals(2, activityPresenter.attachViewInvocations)
		assertNotNull(activityPresenter.attachedView)
		assertTrue(activityPresenter.attachedView !== activity)
		assertEquals(1, fragmentPresenter.detachViewInvocations)
		assertEquals(0, fragmentPresenter.unbindIntentInvocations)
		assertEquals(0, fragmentPresenter.destroyInvocations)
		assertEquals(1, ContainerMviLifecycleFragment.createPresenterInvocations)
		assertEquals(2, fragmentPresenter.attachViewInvocations)
		assertEquals(1, fragmentPresenter.bindIntentInvocations)
		assertNotNull(fragmentPresenter.attachedView)
		assertTrue(fragmentPresenter.attachedView !== fragment)
		assertEquals(1, childFragmentPresenter.detachViewInvocations)
		assertEquals(0, childFragmentPresenter.unbindIntentInvocations)
		assertEquals(0, childFragmentPresenter.destroyInvocations)
		assertEquals(1, MviLifecycleChildFragment.createPresenterInvocations)
		assertEquals(2, childFragmentPresenter.attachViewInvocations)
		assertEquals(1, childFragmentPresenter.bindIntentInvocations)
		assertNotNull(childFragmentPresenter.attachedView)
		assertTrue(childFragmentPresenter.attachedView !== fragment)

		// Press back button --> Activity finishes
		MviLifecycleChildFragmentActivity.pressBackButton()
		sleep(1000)
	}
}