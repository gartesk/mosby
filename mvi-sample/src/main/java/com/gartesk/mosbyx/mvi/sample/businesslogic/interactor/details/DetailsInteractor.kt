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
package com.gartesk.mosbyx.mvi.sample.businesslogic.interactor.details

import com.gartesk.mosbyx.mvi.sample.businesslogic.ShoppingCart
import com.gartesk.mosbyx.mvi.sample.businesslogic.http.ProductBackendApiDecorator
import com.gartesk.mosbyx.mvi.sample.businesslogic.model.FeedItem.Product
import com.gartesk.mosbyx.mvi.sample.businesslogic.model.ProductDetail
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

/**
 * Interactor that is responsible to load product details
 */
class DetailsInteractor(
	private val backendApi: ProductBackendApiDecorator,
	private val shoppingCart: ShoppingCart
) {

	/**
	 * Get the details of a given product
	 */
	fun getDetails(productId: Int): Observable<ProductDetailsViewState> =
		getProductWithShoppingCartInfo(productId)
			.subscribeOn(Schedulers.io())
			.map<ProductDetailsViewState> {
				ProductDetailsViewState.DataState(
					it
				)
			}
			.startWith(ProductDetailsViewState.LoadingState)
			.onErrorReturn {
				ProductDetailsViewState.ErrorState(
					it
				)
			}

	private fun getProductWithShoppingCartInfo(productId: Int): Observable<ProductDetail> =
		Observable.combineLatest(
			backendApi.getProduct(productId),
			shoppingCart.getItemsInShoppingCart(),
			BiFunction { product, productsInShoppingCart ->
				val inShoppingCart = productsInShoppingCart.any { it.id == productId }
				ProductDetail(
					product,
					inShoppingCart
				)
			}
		)

	fun addToShoppingCart(product: Product): Completable =
		shoppingCart.addProduct(product)

	fun removeFromShoppingCart(product: Product): Completable =
		shoppingCart.removeProduct(product)

}