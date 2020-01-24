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
package com.gartesk.mosbyx.mvi.sample.businesslogic.interactor.search

import com.gartesk.mosbyx.mvi.sample.businesslogic.interactor.search.SearchViewState.*
import com.gartesk.mosbyx.mvi.sample.businesslogic.model.FeedItem.Product
import com.gartesk.mosbyx.mvi.sample.businesslogic.searchengine.SearchEngine
import io.reactivex.Observable

/**
 * Interacts with [SearchEngine] to search for items
 */
class SearchInteractor(private val searchEngine: SearchEngine) {

	/**
	 * Search for items
	 */
	fun search(searchString: String): Observable<SearchViewState> =
		if (searchString.isEmpty()) {
			Observable.just(SearchNotStartedYet)
		} else searchEngine.searchFor(searchString)
			.map<SearchViewState> { products: List<Product> ->
				if (products.isEmpty()) {
					EmptyResult(searchString)
				} else {
					SearchResult(searchString, products)
				}
			}
			.startWith(Loading)
			.onErrorReturn { Error(searchString, it) }
}