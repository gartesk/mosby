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
package com.gartesk.mosbyx.sample.mvi.dependencyinjection

import com.gartesk.mosbyx.sample.mvi.businesslogic.ShoppingCart
import com.gartesk.mosbyx.sample.mvi.businesslogic.feed.GroupedPagedFeedLoader
import com.gartesk.mosbyx.sample.mvi.businesslogic.feed.HomeFeedLoader
import com.gartesk.mosbyx.sample.mvi.businesslogic.feed.PagingFeedLoader
import com.gartesk.mosbyx.sample.mvi.businesslogic.http.ProductBackendApi
import com.gartesk.mosbyx.sample.mvi.businesslogic.http.ProductBackendApiDecorator
import com.gartesk.mosbyx.sample.mvi.businesslogic.interactor.details.DetailsInteractor
import com.gartesk.mosbyx.sample.mvi.businesslogic.interactor.search.SearchInteractor
import com.gartesk.mosbyx.sample.mvi.businesslogic.searchengine.SearchEngine
import com.gartesk.mosbyx.sample.mvi.view.product.category.CategoryPresenter
import com.gartesk.mosbyx.sample.mvi.view.checkoutbutton.CheckoutButtonPresenter
import com.gartesk.mosbyx.sample.mvi.view.detail.ProductDetailsPresenter
import com.gartesk.mosbyx.sample.mvi.view.home.HomePresenter
import com.gartesk.mosbyx.sample.mvi.view.menu.MainMenuPresenter
import com.gartesk.mosbyx.sample.mvi.view.product.search.SearchPresenter
import com.gartesk.mosbyx.sample.mvi.view.selectedcounttoolbar.SelectedCountToolbarPresenter
import com.gartesk.mosbyx.sample.mvi.view.shoppingcartlabel.ShoppingCartLabelPresenter
import com.gartesk.mosbyx.sample.mvi.view.shoppingcartoverview.ShoppingCartOverviewItem
import com.gartesk.mosbyx.sample.mvi.view.shoppingcartoverview.ShoppingCartOverviewPresenter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * This is just a very simple example that creates dependency injection.
 * In a real project you might would like to use dagger.
 */
class DependencyInjection {

	companion object {
		var BASE_URL = "https://raw.githubusercontent.com"
		const val BASE_URL_BRANCH = "master"
		val BASE_IMAGE_URL = "$BASE_URL/sockeqwe/mosby/$BASE_URL_BRANCH/sample-mvi/server/images/"
	}

	// Don't do this in your real app
	val clearSelectionRelay = PublishSubject.create<Unit>()

	private val deleteSelectionRelay = PublishSubject.create<Unit>()
	// Some singletons
	private val httpLogger = HttpLoggingInterceptor()

	private val retrofit = Retrofit.Builder()
		.baseUrl(BASE_URL)
		.client(
			OkHttpClient.Builder()
				.addInterceptor(httpLogger)
				.build()
		)
		.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
		.addConverterFactory(MoshiConverterFactory.create())
		.build()
	private val backendApi = retrofit.create(ProductBackendApi::class.java)

	private val backendApiDecorator = ProductBackendApiDecorator(backendApi)

	/**
	 * This is a singleton
	 */
	val mainMenuPresenter = MainMenuPresenter(backendApiDecorator)
	private val shoppingCart = ShoppingCart()

	/**
	 * This is a singleton
	 */
	val shoppingCartPresenter = ShoppingCartOverviewPresenter(
		shoppingCart,
		deleteSelectionRelay,
		clearSelectionRelay
	)

	private fun newSearchEngine(): SearchEngine = SearchEngine(backendApiDecorator)

	private fun newSearchInteractor(): SearchInteractor = SearchInteractor(newSearchEngine())

	fun newPagingFeedLoader(): PagingFeedLoader = PagingFeedLoader(backendApiDecorator)

	fun newGroupedPagedFeedLoader(): GroupedPagedFeedLoader =
		GroupedPagedFeedLoader(newPagingFeedLoader())

	fun newHomeFeedLoader(): HomeFeedLoader =
		HomeFeedLoader(newGroupedPagedFeedLoader(), backendApiDecorator)

	fun newSearchPresenter(): SearchPresenter = SearchPresenter(newSearchInteractor())

	fun newHomePresenter(): HomePresenter = HomePresenter(newHomeFeedLoader())

	fun newCategoryPresenter(): CategoryPresenter = CategoryPresenter(backendApiDecorator)

	fun newProductDetailsPresenter(): ProductDetailsPresenter =
		ProductDetailsPresenter(DetailsInteractor(backendApiDecorator, shoppingCart))

	fun newShoppingCartLabelPresenter(): ShoppingCartLabelPresenter =
		ShoppingCartLabelPresenter(shoppingCart)

	fun newCheckoutButtonPresenter(): CheckoutButtonPresenter =
		CheckoutButtonPresenter(shoppingCart)

	fun newSelectedCountToolbarPresenter(): SelectedCountToolbarPresenter {
		val selectedItemCountObservable: Observable<Int> =
			shoppingCartPresenter.viewStateObservable
				.map { items: List<ShoppingCartOverviewItem> ->
					items.count { it.selected }
				}

		return SelectedCountToolbarPresenter(
			selectedItemCountObservable,
			clearSelectionRelay,
			deleteSelectionRelay
		)
	}
}