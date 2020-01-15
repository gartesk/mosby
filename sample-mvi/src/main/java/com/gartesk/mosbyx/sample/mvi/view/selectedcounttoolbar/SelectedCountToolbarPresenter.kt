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
package com.gartesk.mosbyx.sample.mvi.view.selectedcounttoolbar

import com.gartesk.mosbyx.mvi.MviBasePresenter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SelectedCountToolbarPresenter(
	private val selectedCountObservable: Observable<Int>,
	private val clearSelectionRelay: PublishSubject<Unit>,
	private val deleteSelectedItemsRelay: PublishSubject<Unit>
) : MviBasePresenter<SelectedCountToolbarView, Int>() {

	private var clearSelectionDisposal: Disposable? = null
	private var deleteSelectedItemsDisposal: Disposable? = null

	override fun bindIntents() {
		clearSelectionDisposal = intent { it.clearSelectionIntent() }
				.doOnNext { Timber.d("intent: clear selection") }
				.subscribe { clearSelectionRelay.onNext(Unit) }
		deleteSelectedItemsDisposal =
			intent { it.deleteSelectedItemsIntent() }
				.doOnNext { Timber.d("intent: delete selected items $it") }
				.subscribe { deleteSelectedItemsRelay.onNext(Unit) }

		subscribeViewState(selectedCountObservable) { view, selectedCount ->
			view.render(selectedCount)
		}
	}

	override fun unbindIntents() {
		clearSelectionDisposal?.dispose()
		deleteSelectedItemsDisposal?.dispose()
	}

}