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
package com.gartesk.mosbyx.mvi.integrationtest.lifecycle.viewgroup

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.gartesk.mosbyx.ViewGroupMviDelegateImpl.Companion.DEBUG
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class MviViewGroupFinishOnCreateTest {

	@get:Rule
	var rule = ActivityTestRule(ViewGroupFinishOnCreateContainerActivity::class.java)

	@Test
	fun testFinishInOnCreate() {
		val portraitActivity = rule.activity
		DEBUG = true
		val layout = portraitActivity.layout
		sleep(2000)
		assertNull(layout.presenter)
		assertEquals(0, layout.createPresenterInvocations)
	}
}