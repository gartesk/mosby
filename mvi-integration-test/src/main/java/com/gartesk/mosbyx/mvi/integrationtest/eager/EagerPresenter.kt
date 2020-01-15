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
package com.gartesk.mosbyx.mvi.integrationtest.eager

import com.gartesk.mosbyx.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class EagerPresenter : MviBasePresenter<EagerView, String>() {

	override fun bindIntents() {
		val intent1 = intent { it.intent1() }
			.concatMap { s ->
				Observable.fromCallable {
					Thread.sleep(300)
					"$s - Result 1"
				}.subscribeOn(Schedulers.io())
			}
		val intent2 = intent { it.intent2() }
			.flatMap { s ->
				Observable.just("$s - Result 2")
					.subscribeOn(Schedulers.io())
			}
		val data = Observable.concat(intent1, intent2)
			.observeOn(AndroidSchedulers.mainThread())

		subscribeViewState(data) { view, viewState ->
			view.render(viewState)
		}
	}
}