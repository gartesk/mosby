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
package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.http

import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.Product
import io.reactivex.Observable
import io.reactivex.functions.Function4

/**
 * Since this app only has a static backend providing some static json responses,
 * we have to calculate some things locally on the app users device, that otherwise would be done
 * on
 * a real backend server.
 *
 * All app components should interact with this decorator class and not with the real retrofit
 * interface.
 */
class ProductBackendApiDecorator(private val api: ProductBackendApi) {

	fun getProducts(pagination: Int): Observable<List<Product>> =
		api.getProducts(pagination)

	/**
	 * Get a list with all products from backend
	 */
	fun getProducts(): Observable<List<Product>> =
		Observable.zip(
			getProducts(0),
			getProducts(1),
			getProducts(2),
			getProducts(3),
			Function4 { products0, products1, products2, products3 ->
				products0 + products1 + products2 + products3
			}
		)

	/**
	 * Get all products of a certain category
	 *
	 * @param categoryName The name of the category
	 */
	fun getAllProductsOfCategory(categoryName: String): Observable<List<Product>> =
		getProducts()
			.flatMap { Observable.fromIterable(it) }
			.filter { it.category == categoryName }
			.toList()
			.toObservable()

	/**
	 * Get a list with all categories
	 */
	fun getAllCategories(): Observable<List<String>> =
		getProducts().map { products: List<Product> ->
			products.map { it.category }.distinct()
		}

	/**
	 * Get the product with the given id
	 *
	 * @param productId The product id
	 */
	fun getProduct(productId: Int): Observable<Product> =
		getProducts().flatMap { Observable.fromIterable(it) }
			.filter { it.id == productId }
			.take(1)
}