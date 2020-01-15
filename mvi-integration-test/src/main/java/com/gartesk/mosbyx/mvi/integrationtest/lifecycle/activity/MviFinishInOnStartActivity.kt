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
package com.gartesk.mosbyx.mvi.integrationtest.lifecycle.activity

import com.gartesk.mosbyx.mvi.MviActivity
import com.gartesk.mosbyx.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.gartesk.mosbyx.mvi.integrationtest.lifecycle.LifecycleTestView
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject

class MviFinishInOnStartActivity :
	MviActivity<LifecycleTestView, LifecycleTestPresenter>(), LifecycleTestView {

	var onDestroyReached: Subject<Unit> = ReplaySubject.create()

	override fun createPresenter(): LifecycleTestPresenter {
		return LifecycleTestPresenter()
	}

	override fun onStart() {
		super.onStart()
		finish() // finish  --> this is what we would like to test --> App should not crash
	}

	override fun onDestroy() {
		super.onDestroy()
		onDestroyReached.onNext(Unit)
		onDestroyReached.onComplete()
	}
}