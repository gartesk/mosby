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
package com.gartesk.mosbyx.sample.mvi.businesslogic.searchengine

import com.gartesk.mosbyx.sample.mvi.businesslogic.http.ProductBackendApiDecorator
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.FeedItem.Product
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * With this class you can search for products
 */
class SearchEngine(private val backend: ProductBackendApiDecorator) {

	fun searchFor(query: String): Observable<List<Product>> =
		if (query.isEmpty()) {
			Observable.error(
				IllegalArgumentException(
					"SearchQueryTest is blank"
				)
			)
		} else backend.getProducts()
			.delay(1000, TimeUnit.MILLISECONDS)
			.flatMap { Observable.fromIterable(it) }
			.filter { isProductMatchingSearchCriteria(it, query) }
			.toList()
			.toObservable()

	/**
	 * Filters those items that contains the search query text in name, description or category
	 */
	private fun isProductMatchingSearchCriteria(product: Product, query: String): Boolean =
		query.split(" ")
			.any { word ->
				product.name.contains(word)
						|| product.description.contains(word)
						|| product.category.contains(word)
			}

}