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
package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.fragment

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R

class RetainingFragmentContainerActivity : AppCompatActivity() {

	companion object {
		private const val TAG = "Test-Fragment"
		private lateinit var currentInstance: Activity

		fun pressBackButton() {
			currentInstance.runOnUiThread { currentInstance.onBackPressed() }
		}
	}

	val fragment: SimpleRetainingMviLifecycleFragment
		get() = supportFragmentManager.findFragmentByTag(TAG) as SimpleRetainingMviLifecycleFragment

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		currentInstance = this
		setContentView(R.layout.activity_lifecycle)
		if (savedInstanceState == null) {
			val fragment = SimpleRetainingMviLifecycleFragment()
			fragment.retainInstance = true
			supportFragmentManager
				.beginTransaction()
				.replace(R.id.container, fragment, TAG)
				.commitNow()
		}
	}
}