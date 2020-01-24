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
package com.gartesk.mosbyx.mvi.sample.view.detail

import com.gartesk.mosbyx.mvi.MviBasePresenter
import com.gartesk.mosbyx.mvi.sample.businesslogic.interactor.details.DetailsInteractor
import com.gartesk.mosbyx.mvi.sample.businesslogic.interactor.details.ProductDetailsViewState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class ProductDetailsPresenter(private val interactor: DetailsInteractor) :
	MviBasePresenter<ProductDetailsView, ProductDetailsViewState>() {

	override fun bindIntents() {
		intent { it.addToShoppingCartIntent() }
			.doOnNext { Timber.d("intent: add to shopping cart $it") }
			.flatMap { interactor.addToShoppingCart(it).toObservable<Any>() }
			.subscribe()

		intent { it.removeFromShoppingCartIntent() }
			.doOnNext { Timber.d("intent: remove from shopping cart $it") }
			.flatMap { interactor.removeFromShoppingCart(it).toObservable<Any>() }
			.subscribe()

		val loadDetails: Observable<ProductDetailsViewState> = intent { it.loadDetailsIntent() }
			.doOnNext { Timber.d("intent: load details for product id = $it") }
			.flatMap { interactor.getDetails(it) }
			.observeOn(AndroidSchedulers.mainThread())

		subscribeViewState(loadDetails) { view, viewState ->
			view.render(viewState)
		}
	}

}