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
package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestView

class MviLifecycleActivity :
	MviActivity<LifecycleTestView, LifecycleTestPresenter>(), LifecycleTestView {

	companion object {

		var createPresenterInvocations = 0
		lateinit var currentInstance: Activity

		fun pressBackButton() {
			currentInstance.runOnUiThread { currentInstance.onBackPressed() }
		}
	}

    lateinit var presenter: LifecycleTestPresenter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_lifecycle)
		Log.d(javaClass.simpleName, "onCreate() $this")
		currentInstance = this
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.d(javaClass.simpleName, "onDestroy() $this")
	}

	override fun createPresenter(): LifecycleTestPresenter {
		createPresenterInvocations++
		presenter = LifecycleTestPresenter()
		Log.d(javaClass.simpleName, "createPresenter() $this $presenter")
		return presenter
	}
}