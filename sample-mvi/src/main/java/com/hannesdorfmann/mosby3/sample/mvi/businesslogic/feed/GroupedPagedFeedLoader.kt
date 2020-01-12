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

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.*
import io.reactivex.Observable
import io.reactivex.observables.GroupedObservable

/**
 * Takes a [PagingFeedLoader] but groups the resulting list of products into categories.
 * Assumption: Since we support pagination, we assume that there are not items of the same group on
 * different pages in the data feed retrieved from backend
 */
class GroupedPagedFeedLoader(
	private val feedLoader: PagingFeedLoader,
	private val collapsedGroupProductItemCount: Int = 3
) {

	val groupedFirstPage: Observable<List<FeedItem>>
		get() = groupedNextPage

	val groupedNextPage: Observable<List<FeedItem>>
		get() = groupByCategory(feedLoader.nextPage())

	val newestPage: Observable<List<FeedItem>>
		get() = groupByCategory(feedLoader.newestPage())

	private fun groupByCategory(
		originalListToGroup: Observable<List<Product>>
	): Observable<List<FeedItem>> =
		originalListToGroup.flatMap { Observable.fromIterable(it) }
			.groupBy { it.category }
			.flatMap { groupedByCategory: GroupedObservable<String, Product> ->
				groupedByCategory
					.toList()
					.map { productsInGroup: List<Product> ->
						createFeedItems(groupedByCategory.key ?: "", productsInGroup)
					}
					.toObservable()
			}
			.concatMap { Observable.fromIterable(it) }
			.toList()
			.toObservable()

	private fun createFeedItems(groupName: String, productsInGroup: List<Product>): List<FeedItem> {
		val items = mutableListOf<FeedItem>()
		items.add(SectionHeader(groupName))
		if (collapsedGroupProductItemCount < productsInGroup.size) {
			for (i in 0 until collapsedGroupProductItemCount) {
				items.add(productsInGroup[i])
			}
			items.add(
				AdditionalItemsLoadable(
					productsInGroup.size - collapsedGroupProductItemCount,
					groupName,
					false,
					null
				)
			)
		} else {
			items.addAll(productsInGroup)
		}
		return items
	}
}