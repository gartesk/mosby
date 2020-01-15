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
package com.hannesdorfmann.mosby3.sample.mvi.view.home

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.feed.HomeFeedLoader
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.AdditionalItemsLoadable
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.SectionHeader
import com.hannesdorfmann.mosby3.sample.mvi.view.home.PartialStateChanges.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class HomePresenter(private val feedLoader: HomeFeedLoader) :
	MviBasePresenter<HomeView, HomeViewState>() {

	override fun bindIntents() {
		val loadFirstPage: Observable<PartialStateChanges> = intent { it.loadFirstPageIntent() }
			.doOnNext { Timber.d("intent: load first page") }
			.flatMap {
				feedLoader.loadFirstPage()
					.map<PartialStateChanges> { FirstPageLoaded(it) }
					.startWith(FirstPageLoading)
					.onErrorReturn { FirstPageError(it) }
					.subscribeOn(Schedulers.io())
			}

		val loadNextPage: Observable<PartialStateChanges> = intent { it.loadNextPageIntent() }
			.doOnNext { Timber.d("intent: load next page") }
			.flatMap {
				feedLoader.loadNextPage()
					.map<PartialStateChanges> { NextPageLoaded(it) }
					.startWith(NextPageLoading)
					.onErrorReturn { NexPageLoadingError(it) }
					.subscribeOn(Schedulers.io())
			}

		val pullToRefresh: Observable<PartialStateChanges> = intent { it.pullToRefreshIntent() }
			.doOnNext { Timber.d("intent: pull to refresh") }
			.flatMap {
				feedLoader.loadNewestPage()
					.map<PartialStateChanges> { PullToRefreshLoaded(it) }
					.startWith(PullToRefreshLoading)
					.onErrorReturn { PullToRefreshLoadingError(it) }
					.subscribeOn(Schedulers.io())
			}

		val loadMoreFromGroup: Observable<PartialStateChanges> =
			intent { it.loadAllProductsFromCategoryIntent() }
				.doOnNext { Timber.d("intent: load more from category $it") }
				.flatMap { category ->
					feedLoader.loadProductsOfCategory(category)
						.map<PartialStateChanges> { products ->
							ProductsOfCategoryLoaded(category, products)
						}
						.startWith(ProductsOfCategoryLoading(category))
						.onErrorReturn { ProductsOfCategoryLoadingError(category, it) }
						.subscribeOn(Schedulers.io())
				}

		val allIntentsObservable =
			Observable.merge(loadFirstPage, loadNextPage, pullToRefresh, loadMoreFromGroup)
				.observeOn(AndroidSchedulers.mainThread())

		val initialState = HomeViewState(loadingFirstPage = true)

		val viewStateObservable = allIntentsObservable
			.scan(initialState) { previousState, partialChanges ->
				viewStateReducer(previousState, partialChanges)
			}
			.distinctUntilChanged()

		subscribeViewState(viewStateObservable) { view, viewState ->
			view.render(viewState)
		}
	}

	private fun viewStateReducer(
		previousState: HomeViewState,
		partialChanges: PartialStateChanges
	): HomeViewState =
		when (partialChanges) {
			is FirstPageLoading -> {
				previousState.copy(loadingFirstPage = true, firstPageError = null)
			}

			is FirstPageError -> {
				previousState.copy(loadingFirstPage = false, firstPageError = partialChanges.error)
			}

			is FirstPageLoaded -> {
				previousState.copy(
					loadingFirstPage = false,
					firstPageError = null,
					data = partialChanges.data
				)
			}

			is NextPageLoading -> {
				previousState.copy(loadingNextPage = true, nextPageError = null)
			}

			is NexPageLoadingError -> {
				previousState.copy(loadingNextPage = false, nextPageError = partialChanges.error)
			}

			is NextPageLoaded -> {
				previousState.copy(
					loadingNextPage = false,
					nextPageError = null,
					data = previousState.data + partialChanges.data
				)
			}

			is PullToRefreshLoading -> {
				previousState.copy(loadingPullToRefresh = true, pullToRefreshError = null)
			}

			is PullToRefreshLoadingError -> {
				previousState.copy(
					loadingPullToRefresh = false,
					pullToRefreshError = partialChanges.error
				)
			}

			is PullToRefreshLoaded -> {
				previousState.copy(
					loadingPullToRefresh = false,
					pullToRefreshError = null,
					data = partialChanges.data + previousState.data
				)
			}

			is ProductsOfCategoryLoading -> {
				val (previousAdditionalItemIndex, previousAdditionalItem) =
					findAdditionalItemsLoadable(partialChanges.categoryName, previousState.data)
				val newAdditionalItem = previousAdditionalItem
					.copy(loading = true, loadingError = null)

				val data = previousState.data.toMutableList()
				data[previousAdditionalItemIndex] = newAdditionalItem
				previousState.copy(data = data)
			}

			is ProductsOfCategoryLoadingError -> {
				val (previousAdditionalItemIndex, previousAdditionalItem) =
					findAdditionalItemsLoadable(partialChanges.categoryName, previousState.data)
				val newAdditionalItem = previousAdditionalItem
					.copy(loading = false, loadingError = partialChanges.error)

				val data = previousState.data.toMutableList()
				data[previousAdditionalItemIndex] = newAdditionalItem
				previousState.copy(data = data)
			}

			is ProductsOfCategoryLoaded -> {
				val (previousAdditionalItemIndex) =
					findAdditionalItemsLoadable(partialChanges.categoryName, previousState.data)

				val data = previousState.data.toMutableList()

				// Search for the section header
				var sectionHeaderIndex = -1
				for (i in previousAdditionalItemIndex downTo 0) {
					val item = previousState.data[i]
					if (item is SectionHeader && item.name == partialChanges.categoryName) {
						sectionHeaderIndex = i
						break
					}
					// Remove all items of that category. The new list of products will be added afterwards
					data.removeAt(i)
				}
				if (sectionHeaderIndex < 0) {
					throw RuntimeException("Couldn't find the section header for category ${partialChanges.categoryName}")
				}
				data.addAll(sectionHeaderIndex + 1, partialChanges.data)
				previousState.copy(data = data)
			}
		}

	/**
	 * find the [AdditionalItemsLoadable] for the given category name
	 *
	 * @param categoryName The name of the category
	 * @param items the list of feeditems
	 */
	private fun findAdditionalItemsLoadable(
		categoryName: String,
		items: List<FeedItem>
	): Pair<Int, AdditionalItemsLoadable> =
		items.mapIndexed { index, feedItem -> index to feedItem }
			.find { (_, item) -> item is AdditionalItemsLoadable && item.categoryName == categoryName }
			?.let { (index, item) -> index to item as AdditionalItemsLoadable }
			?: throw RuntimeException("No additional item has been found for category = $categoryName")
}