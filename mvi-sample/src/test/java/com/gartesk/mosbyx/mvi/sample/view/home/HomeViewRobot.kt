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
package com.gartesk.mosbyx.mvi.sample.view.home

import com.gartesk.mosbyx.mvi.sample.businesslogic.model.FeedItem
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import org.junit.Assert.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 * This class is responsible to drive the HomeView.
 * Internally this creates a [HomeView] and attaches it to the [HomePresenter]
 * and offers public API to fire view intents and to check for expected view.render() events.
 */
class HomeViewRobot(presenter: HomePresenter) {

	private val loadFirstPageSubject = PublishSubject.create<Unit>()
	private val loadNextPageSubject = PublishSubject.create<Unit>()
	private val pullToRefreshSubject = PublishSubject.create<Unit>()
	private val loadAllProductsFromCategorySubject = PublishSubject.create<String>()
	private val renderEvents: MutableList<HomeViewState> = CopyOnWriteArrayList()
	private val renderEventSubject = ReplaySubject.create<HomeViewState>()

	init {
		val view: HomeView = object :
			HomeView {
			override fun loadFirstPageIntent(): Observable<Unit> = loadFirstPageSubject

			override fun loadNextPageIntent(): Observable<Unit> = loadNextPageSubject

			override fun pullToRefreshIntent(): Observable<Unit> = pullToRefreshSubject

			override fun loadAllProductsFromCategoryIntent(): Observable<String> =
				loadAllProductsFromCategorySubject

			override fun render(viewState: HomeViewState) {
				renderEvents.add(viewState)
				renderEventSubject.onNext(viewState)
			}
		}

		presenter.attachView(view)
	}

	fun fireLoadFirstPageIntent() {
		loadFirstPageSubject.onNext(Unit)
	}

	fun fireLoadNextPageIntent() {
		loadNextPageSubject.onNext(Unit)
	}

	fun firePullToRefreshIntent() {
		pullToRefreshSubject.onNext(Unit)
	}

	fun fireLoadAllProductsFromCategory(category: String) {
		loadAllProductsFromCategorySubject.onNext(category)
	}

	/**
	 * Blocking waits for view.render() calls and
	 *
	 * @param expectedHomeViewStates The expected  HomeViewStates that will be passed to
	 * view.render()
	 */
	fun assertViewStateRendered(vararg expectedHomeViewStates: HomeViewState) {
		val eventsCount = expectedHomeViewStates.size
		renderEventSubject.take(eventsCount.toLong())
			.timeout(10, TimeUnit.SECONDS)
			.blockingSubscribe()

		if (renderEventSubject.values.size > eventsCount) {
			fail(
				"Expected to wait for $eventsCount, but there were " +
						"${renderEventSubject.values.size} Events in total, which is " +
						"more than expected: ${renderEventSubject.values}"
			)
		}

		expectedHomeViewStates.forEachIndexed { index, homeViewState ->
			val actualHomeViewSTate = renderEvents[index]
			if (!homeViewState.isSoftlyEqualTo(actualHomeViewSTate)) {
				fail("\nExpected: $homeViewState\nActual:   $actualHomeViewSTate")
			}
		}
	}

	private fun HomeViewState.isSoftlyEqualTo(other: HomeViewState): Boolean {
		var dataSoftlyEqual = true
		this.data.forEachIndexed { index, feedItem ->
			if (!feedItem.isSoftlyEqualTo(other.data[index])) {
				dataSoftlyEqual = false
				return@forEachIndexed
			}
		}
		return dataSoftlyEqual
				&& this.loadingFirstPage == other.loadingFirstPage
				&& this.firstPageError?.javaClass == other.firstPageError?.javaClass
				&& this.loadingNextPage == other.loadingNextPage
				&& this.nextPageError?.javaClass == other.nextPageError?.javaClass
				&& this.loadingPullToRefresh == other.loadingPullToRefresh
				&& this.pullToRefreshError?.javaClass == other.pullToRefreshError?.javaClass
	}

	private fun FeedItem.isSoftlyEqualTo(other: FeedItem): Boolean =
		if (this is FeedItem.AdditionalItemsLoadable && other is FeedItem.AdditionalItemsLoadable) {
			this.moreItemsCount == other.moreItemsCount
					&& this.categoryName == other.categoryName
					&& this.loading == other.loading
					&& this.loadingError?.javaClass == other.loadingError?.javaClass
		} else {
			this == other
		}
}