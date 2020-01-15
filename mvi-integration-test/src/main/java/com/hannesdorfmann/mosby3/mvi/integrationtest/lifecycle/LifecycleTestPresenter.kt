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
package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle

import android.util.Log
import com.hannesdorfmann.mosby3.mvi.MviBasePresenter

class LifecycleTestPresenter : MviBasePresenter<LifecycleTestView, Any>() {

    var attachViewInvocations = 0
    var attachedView: LifecycleTestView? = null
    var detachViewInvocations = 0
    var bindIntentInvocations = 0
    var unbindIntentInvocations = 0
    var destroyInvocations = 0

	init {
		Log.d(javaClass.simpleName, "constructor $attachViewInvocations $attachedView in ${toString()}")
	}

	override fun attachView(view: LifecycleTestView) {
		super.attachView(view)
		attachViewInvocations++
		attachedView = view
		Log.d(javaClass.simpleName, "attachView $attachViewInvocations $attachedView in ${toString()}")
	}

	override fun detachView() {
		super.detachView()
		attachedView = null
		detachViewInvocations++
		Log.d(javaClass.simpleName, "detachView $detachViewInvocations in ${toString()}")
	}

	override fun destroy() {
		super.destroy()
		destroyInvocations++
		Log.d(javaClass.simpleName, "destroy Presenter $destroyInvocations in ${toString()}")
	}

	override fun bindIntents() {
		check(bindIntentInvocations < 1) {
			"bindIntents() is called more than once. Invocations: $bindIntentInvocations"
		}
		bindIntentInvocations++
	}

	override fun unbindIntents() {
		super.unbindIntents()
		check(unbindIntentInvocations < 1) {
			"unbindIntents() is called more than once. Invocations: $unbindIntentInvocations"
		}
		unbindIntentInvocations++
	}
}