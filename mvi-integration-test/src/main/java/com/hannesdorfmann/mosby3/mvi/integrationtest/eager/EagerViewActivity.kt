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
package com.hannesdorfmann.mosby3.mvi.integrationtest.eager

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

class EagerViewActivity : MviActivity<EagerView, EagerPresenter>(), EagerView {

    var renderedStrings = ReplaySubject.create<String>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_eager_view)
	}

	override fun intent1(): Observable<String> =
		Observable.just("Intent 1").startWith("Before Intent 1")

	override fun intent2(): Observable<String> = Observable.just("Intent 2")

	override fun render(state: String) {
		findViewById<TextView>(R.id.text).text = state
		Log.d("Render", state)
		renderedStrings.onNext(state)
	}

	override fun createPresenter(): EagerPresenter = EagerPresenter()
}