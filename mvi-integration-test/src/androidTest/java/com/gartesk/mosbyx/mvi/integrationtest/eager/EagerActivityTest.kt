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
package com.gartesk.mosbyx.mvi.integrationtest.eager

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class EagerActivityTest {

	@get:Rule
	var rule = ActivityTestRule(EagerViewActivity::class.java)

	@Test
	fun testStatesRendered() {
		val activity = rule.activity
		val rendered = activity.renderedStrings
			.take(3)
			.timeout(5, TimeUnit.SECONDS)
			.toList()
			.blockingGet()

		assertEquals(
			listOf("Before Intent 1 - Result 1", "Intent 1 - Result 1", "Intent 2 - Result 2"),
			rendered
		)
	}
}