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

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.*
import com.hannesdorfmann.mosby3.sample.mvi.dependencyinjection.DependencyInjection
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import java.lang.reflect.Type
import java.net.ConnectException

/**
 * A simple unit test demonstrating how to write unit tests for MVI Presenters
 */
class HomePresenterTest {

	// Json serializer for mock server
	private val moshi = Moshi.Builder().build()
	private val type: Type = Types.newParameterizedType(
		MutableList::class.java,
		Product::class.java
	)
	private val adapter = moshi.adapter<List<Product>>(type)

	private lateinit var mockWebServer: MockWebServer

	@Before
	fun beforeEachTest() {
		RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
		mockWebServer = MockWebServer()
		mockWebServer.start()
		// Set the apps url to the local mock server
		DependencyInjection.BASE_URL = mockWebServer.url("").toString()
	}

	@After
	fun afterEachTest() {
		RxAndroidPlugins.reset()
		mockWebServer.shutdown()
	}

	@Test
	fun loadingFirstPage() {
		// Prepare mock server to deliver mock response on incoming http request
		val mockProducts = listOf(
			Product(1, "image", "name", "category1", "description", 21.9),
			Product(2, "image", "name", "category1", "description", 21.9),
			Product(3, "image", "name", "category1", "description", 21.9),
			Product(4, "image", "name", "category1", "description", 21.9),
			Product(5, "image", "name", "category1", "description", 21.9)
		)
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProducts)))

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)

		// We are ready, so let's start: fire an intent
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		val expectedData = listOf(
			SectionHeader("category1"),
			mockProducts[0],
			mockProducts[1],
			mockProducts[2],
			AdditionalItemsLoadable(2, "category1", false, null)
		)
		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val firstPage = HomeViewState(data = expectedData)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage)
	}

	@Test
	fun loadingFirstFailsWithNoConnectionError() {
		// Prepare mock server to deliver mock response on incoming http request
		// Simulate no internet connection to the server
		mockWebServer.shutdown()

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)

		// We are ready, so let's start: fire an intent
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show error indicator
		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val errorFirstPage = HomeViewState(firstPageError = ConnectException())

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, errorFirstPage)
	}

	@Test
	fun loadingFirstPageAndNextPage() {
		// Prepare mock server to deliver mock response on incoming http request
		val mockProductsFirstPage = listOf(
			Product(1, "image", "name", "category1", "description", 21.9),
			Product(2, "image", "name", "category1", "description", 21.9),
			Product(3, "image", "name", "category1", "description", 21.9),
			Product(4, "image", "name", "category1", "description", 21.9),
			Product(5, "image", "name", "category1", "description", 21.9)
		)
		val mockProductsNextPage = listOf(
			Product(6, "image", "name", "category2", "description", 21.9),
			Product(7, "image", "name", "category2", "description", 21.9),
			Product(8, "image", "name", "category2", "description", 21.9),
			Product(9, "image", "name", "category2", "description", 21.9)
		)
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsFirstPage)))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsNextPage)))

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)

		// We are ready, so let's start: fire intents
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		val expectedDataAfterFirstPage = listOf(
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null)
		)

		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val firstPage = HomeViewState(data = expectedDataAfterFirstPage)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage)

		// Fire second intent
		robot.fireLoadNextPageIntent()

		// we expect that 4 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator (caused by loadFirstPageIntent)
		// 2. show the items with the first page (caused by loadFirstPageIntent)
		// 3. show loading next page indicator
		// 4. show next page content (plus original first page content)
		val expectedDataAfterNextPage = listOf(
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null),
			SectionHeader("category2"),
			mockProductsNextPage[0],
			mockProductsNextPage[1],
			mockProductsNextPage[2],
			AdditionalItemsLoadable(1, "category2", false, null)
		)
		val nextPageLoading = HomeViewState(
			data = expectedDataAfterFirstPage,
			loadingNextPage = true
		)
		val nextPage = HomeViewState(data = expectedDataAfterNextPage)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage, nextPageLoading, nextPage)
	}

	@Test
	fun loadingFirstPageAndFailLoadingNextPage() {
		// Prepare mock server to deliver mock response on incoming http request
		val mockProductsFirstPage = listOf(
			Product(1, "image", "name", "category1", "description", 21.9),
			Product(2, "image", "name", "category1", "description", 21.9),
			Product(3, "image", "name", "category1", "description", 21.9),
			Product(4, "image", "name", "category1", "description", 21.9),
			Product(5, "image", "name", "category1", "description", 21.9)
		)
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsFirstPage)))

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)

		// We are ready, so let's start: fire intents
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		val expectedDataAfterFirstPage = listOf(
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null)
		)
		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val firstPage = HomeViewState(data = expectedDataAfterFirstPage)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage)

		// Fire second intent
		// causes loading next page error
		mockWebServer.shutdown()
		robot.fireLoadNextPageIntent()

		// we expect that 4 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator (caused by loadFirstPageIntent)
		// 2. show the items with the first page (caused by loadFirstPageIntent)
		// 3. show loading next page indicator
		// 4. show next page error (plus original first page content)
		val nextPageLoading = HomeViewState(
			data = expectedDataAfterFirstPage,
			loadingNextPage = true
		)
		val nextPage = HomeViewState(
			data = expectedDataAfterFirstPage,
			nextPageError = ConnectException()
		)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage, nextPageLoading, nextPage)
	}

	@Test
	fun loadingFirstPageAndNextPageAndPullToRefresh() {
		// Prepare mock server to deliver mock response on incoming http request
		val mockProductsFirstPage = listOf(
			Product(1, "image", "name", "category1", "description", 21.9),
			Product(2, "image", "name", "category1", "description", 21.9),
			Product(3, "image", "name", "category1", "description", 21.9),
			Product(4, "image", "name", "category1", "description", 21.9),
			Product(5, "image", "name", "category1", "description", 21.9)
		)
		val mockProductsNextPage = listOf(
			Product(6, "image", "name", "category2", "description", 21.9),
			Product(7, "image", "name", "category2", "description", 21.9),
			Product(8, "image", "name", "category2", "description", 21.9),
			Product(9, "image", "name", "category2", "description", 21.9)
		)
		val mockProductsPullToRefresh = listOf(
			Product(10, "image", "name", "category3", "description", 21.9),
			Product(11, "image", "name", "category3", "description", 21.9),
			Product(12, "image", "name", "category3", "description", 21.9)
		)
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsFirstPage)))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsNextPage)))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsPullToRefresh)))

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)
		// We are ready, so let's start: fire intents
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		val expectedDataAfterFirstPage = listOf(
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null)
		)
		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val firstPage = HomeViewState(data = expectedDataAfterFirstPage)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage)

		// Fire next page intent
		robot.fireLoadNextPageIntent()

		// we expect that 4 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator (caused by loadFirstPageIntent)
		// 2. show the items with the first page (caused by loadFirstPageIntent)
		// 3. show loading next page indicator
		// 4. show next page content (plus original first page content)
		val expectedDataAfterNextPage = listOf(
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null),
			SectionHeader("category2"),
			mockProductsNextPage[0],
			mockProductsNextPage[1],
			mockProductsNextPage[2],
			AdditionalItemsLoadable(1, "category2", false, null)
		)
		val nextPageLoading = HomeViewState(
			data = expectedDataAfterFirstPage,
			loadingNextPage = true
		)
		val nextPage = HomeViewState(data = expectedDataAfterNextPage)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage, nextPageLoading, nextPage)

		// fire pull to refresh intent
		robot.firePullToRefreshIntent()

		// we expect that 6 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator (caused by loadFirstPageIntent)
		// 2. show the items with the first page (caused by loadFirstPageIntent)
		// 3. show loading next page indicator
		// 4. show next page content (plus original first page content)
		// 5. show loading - pull to refresh indicator
		// 6. show pull to refresh content (plus original first page + next page content)
		val expectedDataAfterPullToRefresh = listOf(
			SectionHeader("category3"),
			mockProductsPullToRefresh[0],
			mockProductsPullToRefresh[1],
			mockProductsPullToRefresh[2],  // No additional items loadable for category3
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null),
			SectionHeader("category2"),
			mockProductsNextPage[0],
			mockProductsNextPage[1],
			mockProductsNextPage[2],
			AdditionalItemsLoadable(1, "category2", false, null)
		)
		val pullToRefreshLoading = HomeViewState(
			data = expectedDataAfterNextPage,
			loadingPullToRefresh = true
		)
		val pullToRefreshPage = HomeViewState(data = expectedDataAfterPullToRefresh)

		// Check if as expected
		robot.assertViewStateRendered(
			loadingFirstPage,
			firstPage,
			nextPageLoading,
			nextPage,
			pullToRefreshLoading,
			pullToRefreshPage
		)
	}

	@Test
	fun loadingFirstPageAndNextPageAndFailPullToRefresh() {
		// Prepare mock server to deliver mock response on incoming http request
		val mockProductsFirstPage = listOf(
			Product(1, "image", "name", "category1", "description", 21.9),
			Product(2, "image", "name", "category1", "description", 21.9),
			Product(3, "image", "name", "category1", "description", 21.9),
			Product(4, "image", "name", "category1", "description", 21.9),
			Product(5, "image", "name", "category1", "description", 21.9)
		)
		val mockProductsNextPage = listOf(
			Product(6, "image", "name", "category2", "description", 21.9),
			Product(7, "image", "name", "category2", "description", 21.9),
			Product(8, "image", "name", "category2", "description", 21.9),
			Product(9, "image", "name", "category2", "description", 21.9)
		)
		val mockProductsPullToRefresh = listOf(
			Product(10, "image", "name", "category3", "description", 21.9),
			Product(11, "image", "name", "category3", "description", 21.9),
			Product(12, "image", "name", "category3", "description", 21.9)
		)
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsFirstPage)))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsNextPage)))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProductsPullToRefresh)))

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)

		// We are ready, so let's start: fire intents
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		val expectedDataAfterFirstPage = listOf(
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null)
		)
		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val firstPage = HomeViewState(data = expectedDataAfterFirstPage)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage)

		// Fire next page intent
		robot.fireLoadNextPageIntent()

		// we expect that 4 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator (caused by loadFirstPageIntent)
		// 2. show the items with the first page (caused by loadFirstPageIntent)
		// 3. show loading next page indicator
		// 4. show next page content (plus original first page content)
		val expectedDataAfterNextPage = listOf(
			SectionHeader("category1"),
			mockProductsFirstPage[0],
			mockProductsFirstPage[1],
			mockProductsFirstPage[2],
			AdditionalItemsLoadable(2, "category1", false, null),
			SectionHeader("category2"),
			mockProductsNextPage[0],
			mockProductsNextPage[1],
			mockProductsNextPage[2],
			AdditionalItemsLoadable(1, "category2", false, null)
		)
		val nextPageLoading = HomeViewState(
			data = expectedDataAfterFirstPage,
			loadingNextPage = true
		)
		val nextPage = HomeViewState(data = expectedDataAfterNextPage)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage, nextPageLoading, nextPage)

		// fire pull to refresh intent
		// Error: no connection to server
		mockWebServer.shutdown()
		robot.firePullToRefreshIntent()

		// we expect that 6 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator (caused by loadFirstPageIntent)
		// 2. show the items with the first page (caused by loadFirstPageIntent)
		// 3. show loading next page indicator
		// 4. show next page content (plus original first page content)
		// 5. show loading - pull to refresh indicator
		// 6. show error loading  pull to refresh (plus original first page + next page content)
		val pullToRefreshLoading = HomeViewState(
			data = expectedDataAfterNextPage,
			loadingPullToRefresh = true
		)
		val pullToRefreshError = HomeViewState(
			data = expectedDataAfterNextPage,
			pullToRefreshError = ConnectException()
		)

		// Check if as expected
		robot.assertViewStateRendered(
			loadingFirstPage,
			firstPage,
			nextPageLoading,
			nextPage,
			pullToRefreshLoading,
			pullToRefreshError
		)
	}

	@Test
	fun loadingFirstPageAndMoreOfCategory() {
		// Prepare mock server to deliver mock response on incoming http request
		val mockProducts = listOf(
			Product(1, "image", "name", "category1", "description", 21.9),
			Product(2, "image", "name", "category1", "description", 21.9),
			Product(3, "image", "name", "category1", "description", 21.9),
			Product(4, "image", "name", "category1", "description", 21.9),
			Product(5, "image", "name", "category1", "description", 21.9)
		)
		// first page response
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProducts)))
		// more of category responses
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProducts)))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(emptyList())))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(emptyList())))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(emptyList())))

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)

		// We are ready, so let's start: fire an intent
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		val expectedData = listOf(
			SectionHeader("category1"),
			mockProducts[0],
			mockProducts[1],
			mockProducts[2],
			AdditionalItemsLoadable(2, "category1", false, null)
		)
		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val firstPage = HomeViewState(data = expectedData)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage)

		// Load more of category
		robot.fireLoadAllProductsFromCategory("category1")

		// we expect that 4 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		// 3. show indicator load more of category
		// 4. show all items of the category
		val expectedDataWhileLoadingMoreOfCategory = listOf(
			SectionHeader("category1"),
			mockProducts[0],
			mockProducts[1],
			mockProducts[2],
			AdditionalItemsLoadable(2, "category1", true, null)
		)
		val expectedDataAfterAllOfCategoryCompleted = listOf(
			SectionHeader("category1"),
			mockProducts[0],
			mockProducts[1],
			mockProducts[2],
			mockProducts[3],
			mockProducts[4]
		)
		val loadingMoreOfCategory = HomeViewState(data = expectedDataWhileLoadingMoreOfCategory)
		val moreOfCategoryLoaded = HomeViewState(data = expectedDataAfterAllOfCategoryCompleted)

		// Check if as expected
		robot.assertViewStateRendered(
			loadingFirstPage,
			firstPage,
			loadingMoreOfCategory,
			moreOfCategoryLoaded
		)
	}

	@Test
	fun loadingFirstPageAndMoreOfCategoryFails() {
		// Prepare mock server to deliver mock response on incoming http request
		val mockProducts = listOf(
			Product(1, "image", "name", "category1", "description", 21.9),
			Product(2, "image", "name", "category1", "description", 21.9),
			Product(3, "image", "name", "category1", "description", 21.9),
			Product(4, "image", "name", "category1", "description", 21.9),
			Product(5, "image", "name", "category1", "description", 21.9)
		)
		// first page response
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProducts)))
		// more of category responses
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(mockProducts)))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(emptyList())))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(emptyList())))
		mockWebServer.enqueue(MockResponse().setBody(adapter.toJson(emptyList())))

		// init the robot to drive to View which triggers intents on the presenter
		// In a real app you could use dagger or instantiate the Presenter manually like new HomePresenter(...)
		val presenter = DependencyInjection().newHomePresenter()
		val robot = HomeViewRobot(presenter)

		// We are ready, so let's start: fire an intent
		robot.fireLoadFirstPageIntent()

		// we expect that 2 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		val expectedData = listOf(
			SectionHeader("category1"),
			mockProducts[0],
			mockProducts[1],
			mockProducts[2],
			AdditionalItemsLoadable(2, "category1", false, null)
		)
		val loadingFirstPage = HomeViewState(loadingFirstPage = true)
		val firstPage = HomeViewState(data = expectedData)

		// Check if as expected
		robot.assertViewStateRendered(loadingFirstPage, firstPage)

		// Load more of category
		// causes loading category error
		mockWebServer.shutdown()
		robot.fireLoadAllProductsFromCategory("category1")

		// we expect that 4 view.render() events happened with the following HomeViewState:
		// 1. show loading indicator
		// 2. show the items with the first page
		// 3. show indicator load more of category
		// 4. show loading all items of the category failed
		val expectedDataWhileLoadingMoreOfCategory = listOf(
			SectionHeader("category1"),
			mockProducts[0],
			mockProducts[1],
			mockProducts[2],
			AdditionalItemsLoadable(2, "category1", true, null)
		)
		val expectedDataAfterLoadingMoreOfCategoryError = listOf(
			SectionHeader("category1"),
			mockProducts[0],
			mockProducts[1],
			mockProducts[2],
			AdditionalItemsLoadable(2, "category1", false, ConnectException())
		)
		val loadingMoreOfCategory = HomeViewState(data = expectedDataWhileLoadingMoreOfCategory)
		val moreOfCategoryError = HomeViewState(data = expectedDataAfterLoadingMoreOfCategoryError)

		// Check if as expected
		robot.assertViewStateRendered(
			loadingFirstPage,
			firstPage,
			loadingMoreOfCategory,
			moreOfCategoryError
		)
	}
}