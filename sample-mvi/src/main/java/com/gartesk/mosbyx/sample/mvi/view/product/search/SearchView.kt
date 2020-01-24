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
package com.gartesk.mosbyx.sample.mvi.view.product.search

import com.gartesk.mosbyx.mvi.MviView
import com.gartesk.mosbyx.sample.mvi.businesslogic.interactor.search.SearchViewState
import io.reactivex.Observable

interface SearchView : MviView {
	/**
	 * The search intent
	 *
	 * @return An observable emitting search query text
	 */
	fun searchIntent(): Observable<String>

	/**
	 * Renders the viewState
	 *
	 * @param viewState The current viewState state that should be displayed
	 */
	fun render(viewState: SearchViewState)
}