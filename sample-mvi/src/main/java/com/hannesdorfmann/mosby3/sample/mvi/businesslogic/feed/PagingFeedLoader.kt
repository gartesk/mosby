/*
 * Copyright 2016 Hannes Dorfmann.
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
 *
 */
package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.feed

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http.ProductBackendApiDecorator
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.Product
import io.reactivex.Observable

class PagingFeedLoader(private val backend: ProductBackendApiDecorator) {

	private var currentPage = 1
	private var endReached = false
	private var newestPageLoaded = false

	fun newestPage(): Observable<List<Product>> =
		if (newestPageLoaded) {
			Observable.fromCallable<List<Product>> {
				Thread.sleep(2000)
				emptyList()
			}
		} else {
			backend.getProducts(0)
				.doOnNext {
					newestPageLoaded = true
				}
		}

	fun nextPage(): Observable<List<Product>> =
		if (endReached) {
			Observable.just<List<Product>>(emptyList())
		} else {
			backend.getProducts(currentPage)
				.doOnNext { result: List<Product> ->
					currentPage++
					if (result.isEmpty()) {
						endReached = true
					}
				}
		}

}