package com.hannesdorfmann.mosby3.mvi.integrationtest.eager

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