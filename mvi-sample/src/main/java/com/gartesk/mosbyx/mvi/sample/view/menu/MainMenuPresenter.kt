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
package com.gartesk.mosbyx.mvi.sample.view.menu

import com.gartesk.mosbyx.mvi.MviBasePresenter
import com.gartesk.mosbyx.mvi.sample.businesslogic.http.ProductBackendApiDecorator
import com.gartesk.mosbyx.mvi.sample.businesslogic.model.MainMenuItem
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MainMenuPresenter(private val backendApi: ProductBackendApiDecorator) :
	MviBasePresenter<MainMenuView, MenuViewState>() {

	override fun bindIntents() {
		val loadCategories: Observable<List<String>> = intent { it.loadCategoriesIntent() }
			.doOnNext { Timber.d("intent: load category $it") }
			.flatMap {
				backendApi.getAllCategories()
					.subscribeOn(Schedulers.io())
			}

		val selectCategory: Observable<String> = intent { it.selectCategoryIntent() }
			.doOnNext { Timber.d("intent: select category $it") }
			.startWith(MainMenuItem.HOME)

		val menuViewStateObservable: Observable<MenuViewState> =
			Observable.combineLatest(
				loadCategories,
				selectCategory,
				BiFunction<List<String>, String, MenuViewState> { categories, selectedCategory ->
					val homeItem =
						MainMenuItem(
							MainMenuItem.HOME,
							selectedCategory == MainMenuItem.HOME
						)
					val items = listOf(homeItem) + categories.map {
						MainMenuItem(
							it,
							it == selectedCategory
						)
					}
					MenuViewState.DataState(
						items
					)
				}
			)
				.startWith(MenuViewState.LoadingState)
				.onErrorReturn {
					MenuViewState.ErrorState(
						it
					)
				}
				.observeOn(AndroidSchedulers.mainThread())

		subscribeViewState(menuViewStateObservable) { view, viewState ->
			view.render(viewState)
		}
	}
}