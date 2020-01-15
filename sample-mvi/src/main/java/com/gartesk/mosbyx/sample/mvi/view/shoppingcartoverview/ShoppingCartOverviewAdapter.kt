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
package com.gartesk.mosbyx.sample.mvi.view.shoppingcartoverview

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.FeedItem.Product
import com.gartesk.mosbyx.sample.mvi.view.detail.ProductDetailsActivity
import com.gartesk.mosbyx.sample.mvi.view.shoppingcartoverview.ShoppingCartItemViewHolder.ItemSelectedListener
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class ShoppingCartOverviewAdapter(private val activity: Activity) :
	RecyclerView.Adapter<ShoppingCartItemViewHolder>(),
	ItemSelectedListener {

	private val layoutInflater: LayoutInflater = activity.layoutInflater
	private var items: List<ShoppingCartOverviewItem> = emptyList()
	private val selectedProducts = PublishSubject.create<List<Product>>()

	private val inSelectionMode: Boolean
		get() = items.any { it.selected }

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ShoppingCartItemViewHolder = ShoppingCartItemViewHolder.create(layoutInflater, this)

	override fun onBindViewHolder(holder: ShoppingCartItemViewHolder, position: Int) {
		holder.bind(items[position])
	}

	override fun getItemCount(): Int = items.size

	override fun onItemClicked(item: ShoppingCartOverviewItem) {
		if (inSelectionMode) {
			toggleSelection(item)
		} else {
			ProductDetailsActivity.start(activity, item.product)
		}
	}

	override fun onItemLongPressed(item: ShoppingCartOverviewItem): Boolean {
		toggleSelection(item)
		return true
	}

	fun setItems(items: List<ShoppingCartOverviewItem>) {
		val beforeItems = this.items
		this.items = items
		val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

			override fun getOldListSize(): Int {
				return beforeItems.size
			}

			override fun getNewListSize(): Int {
				return items.size
			}

			override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
				beforeItems[oldItemPosition].product == items[newItemPosition].product

			override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
				beforeItems[oldItemPosition] == items[newItemPosition]
		})
		diffResult.dispatchUpdatesTo(this)
	}

	private fun toggleSelection(toToggle: ShoppingCartOverviewItem) {
		val selectedItems = items.filter { item ->
			if (item == toToggle) {
				!toToggle.selected
			} else {
				item.selected
			}
		}
			.map { it.product }

		selectedProducts.onNext(selectedItems)
	}

	fun selectedItemsObservable(): Observable<List<Product>> =
		selectedProducts.doOnNext { selected: List<Product> ->
			Timber.d("selected $selected")
		}

	fun getProductAt(position: Int): Product = items[position].product

}