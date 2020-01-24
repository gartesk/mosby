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
package com.gartesk.mosbyx.sample.mvi.view.home

import com.gartesk.mosbyx.mvi.MviView
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.FeedItem
import io.reactivex.Observable

/**
 * The HomeView responsible to display a list of [FeedItem]
 */
interface HomeView : MviView {
	/**
	 * The intent to load the first page
	 */
	fun loadFirstPageIntent(): Observable<Unit>

	/**
	 * The intent to load the next page
	 */
	fun loadNextPageIntent(): Observable<Unit>

	/**
	 * The intent to react on pull-to-refresh
	 */
	fun pullToRefreshIntent(): Observable<Unit>

	/**
	 * The intent to load more items from a given group
	 *
	 * @return Observable with the name of the group
	 */
	fun loadAllProductsFromCategoryIntent(): Observable<String>

	/**
	 * Renders the viewState
	 */
	fun render(viewState: HomeViewState)
}