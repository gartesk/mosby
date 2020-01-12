/*
 * Copyright 2017 Hannes Dorfmann.
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
package com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartoverview

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.ShoppingCart
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.Product
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import timber.log.Timber

class ShoppingCartOverviewPresenter(
	private val shoppingCart: ShoppingCart,
	private val deleteSelectedItemsIntent: Observable<Unit>,
	private val clearSelectionIntent: Observable<Unit>
) : MviBasePresenter<ShoppingCartOverviewView, List<ShoppingCartOverviewItem>>() {

	private var deleteDisposable: Disposable? = null
	private var deleteSelectedDisposable: Disposable? = null

	override fun bindIntents() {
		// Observable that emits a list of selected products over time (or empty list if the selection has been cleared)
		val selectedItemsIntent: Observable<List<Product>> = intent { it.selectItemsIntent() }
			.mergeWith(clearSelectionIntent.map { emptyList<Product>() })
			.doOnNext { items: List<Product> ->
				Timber.d("intent: selected items %d", items.size)
			}
			.startWith(emptyList<Product>())
			.publish()
			.refCount()

		// Delete multiple selected Items
		deleteSelectedDisposable = selectedItemsIntent
			.switchMap { selectedItems: List<Product> ->
				deleteSelectedItemsIntent
					.filter { selectedItems.isNotEmpty() }
					.doOnNext { Timber.d("intent: remove ${selectedItems.size} selected items from shopping cart") }
					.flatMap { shoppingCart.removeProducts(selectedItems).toObservable<Any>() }
			}
			.subscribe()

		// Delete a single item
		deleteDisposable = intent { it.removeItemIntent() }
			.doOnNext { Timber.d("intent: remove item from shopping cart: $it") }
			.flatMap { shoppingCart.removeProduct(it).toObservable<Any>() }
			.subscribe()

		// Display a list of items in the shopping cart
		val shoppingCartContentObservable: Observable<List<Product>> =
			intent { it.loadItemsIntent() }
				.doOnNext { Timber.d("load ShoppingCart intent") }
				.flatMap { shoppingCart.getItemsInShoppingCart() }

		// Display list of items / view state

		val shoppingCartContentWithSelectedItems: Observable<List<ShoppingCartOverviewItem>> =
			Observable.combineLatest<List<Product>, List<Product>, List<ShoppingCartOverviewItem>>(
				shoppingCartContentObservable,
				selectedItemsIntent,
				BiFunction { itemsInShoppingCart: List<Product>, selectedProducts: List<Product> ->
					itemsInShoppingCart.map { ShoppingCartOverviewItem(it, selectedProducts.contains(it)) }
				}
			)
				.observeOn(AndroidSchedulers.mainThread())

		subscribeViewState(shoppingCartContentWithSelectedItems) { view, itemsInShoppingCart ->
			view.render(itemsInShoppingCart)
		}
	}

	override fun unbindIntents() {
		deleteDisposable?.dispose()
		deleteSelectedDisposable?.dispose()
	}
}