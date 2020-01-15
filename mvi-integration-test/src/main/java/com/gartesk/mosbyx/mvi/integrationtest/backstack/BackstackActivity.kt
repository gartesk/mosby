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
package com.gartesk.mosbyx.mvi.integrationtest.backstack

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gartesk.mosbyx.mvi.integrationtest.R
import com.gartesk.mosbyx.mvi.integrationtest.backstack.first.FirstMviFragment
import com.gartesk.mosbyx.mvi.integrationtest.backstack.first.FirstPresenter
import com.gartesk.mosbyx.mvi.integrationtest.backstack.second.SecondMviFragment
import com.gartesk.mosbyx.mvi.integrationtest.backstack.second.SecondPresenter
import java.util.concurrent.atomic.AtomicInteger

class BackstackActivity : AppCompatActivity() {

	companion object {

		var firstPresenter = FirstPresenter()
		var secondPresenter = SecondPresenter()
		var createFirstPresenterCalls = AtomicInteger(0)
		var createSecondPresenterCalls = AtomicInteger(0)
		private lateinit var currentInstance: BackstackActivity

		fun navigateToSecondFragment() {
			currentInstance.runOnUiThread {
				currentInstance.supportFragmentManager
					.beginTransaction()
					.replace(R.id.fragmentContainer, SecondMviFragment())
					.addToBackStack(null)
					.commit()
			}
		}

		fun rotateToLandscape() {
			currentInstance.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		}

		fun rotateToPortrait() {
			currentInstance.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		}

		fun pressBackButton() {
			currentInstance.runOnUiThread { currentInstance.onBackPressed() }
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_backstack)
		currentInstance = this
		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragmentContainer, FirstMviFragment())
				.commit()
		}
	}
}