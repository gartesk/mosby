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
package com.gartesk.mosbyx.mvi.sample.view.product.category

import com.gartesk.mosbyx.mvi.MviBasePresenter
import com.gartesk.mosbyx.mvi.sample.businesslogic.http.ProductBackendApiDecorator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class CategoryPresenter(private val backendApi: ProductBackendApiDecorator) :
	MviBasePresenter<CategoryView, CategoryViewState>() {

	override fun bindIntents() {
		val categoryViewStateObservable: Observable<CategoryViewState> = intent { it.loadIntents() }
			.flatMap { categoryName ->
				backendApi.getAllProductsOfCategory(categoryName)
					.subscribeOn(Schedulers.io())
					.map<CategoryViewState> { products ->
						CategoryViewState.DataState(
							products
						)
					}
					.startWith(CategoryViewState.LoadingState)
					.onErrorReturn {
						CategoryViewState.ErrorState(
							it
						)
					}
			}
			.observeOn(AndroidSchedulers.mainThread())

		subscribeViewState(categoryViewStateObservable) { view, viewState ->
			view.render(viewState)
		}
	}

}