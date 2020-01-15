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
package com.gartesk.mosbyx.mvi.integrationtest.backstack.first

import android.util.Log
import com.gartesk.mosbyx.mvi.MviBasePresenter
import java.util.concurrent.atomic.AtomicInteger

class FirstPresenter : MviBasePresenter<FirstView, Any>() {

    var bindIntentCalls = AtomicInteger(0)
    var unbindIntentCalls = AtomicInteger(0)
    var attachViewCalls = AtomicInteger(0)
    var detachViewCalls = AtomicInteger(0)
	private var destroyCalls = AtomicInteger(0)

	override fun bindIntents() {
		bindIntentCalls.incrementAndGet()
	}

	override fun unbindIntents() {
		super.unbindIntents()
		unbindIntentCalls.incrementAndGet()
	}

	override fun attachView(view: FirstView) {
		super.attachView(view)
		attachViewCalls.incrementAndGet()
	}

	override fun detachView() {
		super.detachView()
		Log.d("Presenters", "First Retain Presenter detachView")
		detachViewCalls.incrementAndGet()
	}

	override fun destroy() {
		super.destroy()
		destroyCalls.incrementAndGet()
	}
}