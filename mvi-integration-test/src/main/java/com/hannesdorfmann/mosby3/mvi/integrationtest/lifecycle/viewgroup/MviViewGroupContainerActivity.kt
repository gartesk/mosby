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

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R

class MviViewGroupContainerActivity : AppCompatActivity() {

	companion object {

		private lateinit var currentInstance: Activity

		val mviViewGroup: TestMviFrameLayout
			get() = currentInstance.findViewById(R.id.testFrameLayout)

		fun pressBackButton() {
			currentInstance.runOnUiThread { currentInstance.onBackPressed() }
		}

		fun removeMviViewGroup() {
			currentInstance.runOnUiThread {
				val rootView = currentInstance.findViewById<ViewGroup>(R.id.rootView)
				rootView.removeAllViews()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		currentInstance = this
		setContentView(R.layout.activity_viewgroup_mvi)
	}
}