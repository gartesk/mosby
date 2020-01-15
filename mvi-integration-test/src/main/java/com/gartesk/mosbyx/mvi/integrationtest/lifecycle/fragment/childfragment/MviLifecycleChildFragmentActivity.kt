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
package com.gartesk.mosbyx.mvi.integrationtest.lifecycle.fragment.childfragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.gartesk.mosbyx.mvi.MviActivity
import com.gartesk.mosbyx.mvi.integrationtest.R
import com.gartesk.mosbyx.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.gartesk.mosbyx.mvi.integrationtest.lifecycle.LifecycleTestView

class MviLifecycleChildFragmentActivity :
	MviActivity<LifecycleTestView, LifecycleTestPresenter>(), LifecycleTestView {

	companion object {
		private const val TAG = "FragmetnTag"

		var createPresenterInvocations = 0

		private lateinit var currentInstance: Activity

		fun pressBackButton() {
			currentInstance.runOnUiThread { currentInstance.onBackPressed() }
		}
	}

    lateinit var presenter: LifecycleTestPresenter

	val fragment: ContainerMviLifecycleFragment
		get() = supportFragmentManager.findFragmentByTag(TAG) as ContainerMviLifecycleFragment

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		currentInstance = this
		setContentView(R.layout.activity_child_mvi_container)
		Log.d(javaClass.simpleName, "onCreate() $this")
		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
				.replace(
					R.id.fragmentContainer,
					ContainerMviLifecycleFragment(),
					TAG
				)
				.commitNow()
		}
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