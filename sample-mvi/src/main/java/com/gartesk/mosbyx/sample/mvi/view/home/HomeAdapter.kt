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
package com.gartesk.mosbyx.sample.mvi.view.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.FeedItem
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.FeedItem.*
import com.gartesk.mosbyx.sample.mvi.view.home.viewholder.LoadingViewHolder
import com.gartesk.mosbyx.sample.mvi.view.home.viewholder.MoreItemsViewHolder
import com.gartesk.mosbyx.sample.mvi.view.home.viewholder.MoreItemsViewHolder.LoadItemsClickListener
import com.gartesk.mosbyx.sample.mvi.view.home.viewholder.SectionHeaderViewHolder
import com.gartesk.mosbyx.sample.mvi.view.product.ProductViewHolder
import com.gartesk.mosbyx.sample.mvi.view.product.ProductViewHolder.ProductClickedListener
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class HomeAdapter(
	private val layoutInflater: LayoutInflater,
	private val productClickedListener: ProductClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
	LoadItemsClickListener {

	companion object {
		const val VIEW_TYPE_PRODUCT = 0
		const val VIEW_TYPE_LOADING_MORE_NEXT_PAGE = 1
		const val VIEW_TYPE_SECTION_HEADER = 2
		const val VIEW_TYPE_MORE_ITEMS_AVAILABLE = 3
	}

	var loadingNextPage = false
		private set

	var items: List<FeedItem> = emptyList()
		private set

	private val loadMoreItemsOfCategoryObservable = PublishSubject.create<String>()

	/**
	 * @return true if value has changed since last invocation
	 */
	fun setLoadingNextPage(loadingNextPage: Boolean): Boolean {
		val hasLoadingMoreChanged = loadingNextPage != this.loadingNextPage
		val notifyInserted = loadingNextPage && hasLoadingMoreChanged
		val notifyRemoved = !loadingNextPage && hasLoadingMoreChanged
		this.loadingNextPage = loadingNextPage
		if (notifyInserted) {
			notifyItemInserted(items.size)
		} else if (notifyRemoved) {
			notifyItemRemoved(items.size)
		}
		return hasLoadingMoreChanged
	}

	fun setItems(newItems: List<FeedItem>) {
		val oldItems = items
		items = newItems
		DiffUtil.calculateDiff(object : DiffUtil.Callback() {
			override fun getOldListSize(): Int {
				return oldItems.size
			}

			override fun getNewListSize(): Int {
				return newItems.size
			}

			override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
				val oldItem: Any = oldItems[oldItemPosition]
				val newItem: Any = newItems[newItemPosition]

				val productsSame = oldItem is Product
						&& newItem is Product
						&& oldItem.id == newItem.id
				val sectionHeadersSame = oldItem is SectionHeader
						&& newItem is SectionHeader
						&& oldItem.name == newItem.name
				val additionalItemsSame = oldItem is AdditionalItemsLoadable
						&& newItem is AdditionalItemsLoadable
						&& oldItem.categoryName == newItem.categoryName

				return productsSame || sectionHeadersSame || additionalItemsSame
			}

			override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
				val oldItem: Any = oldItems[oldItemPosition]
				val newItem: Any = newItems[newItemPosition]
				return oldItem == newItem
			}
		}, true)
			.dispatchUpdatesTo(this)
	}

	override fun getItemViewType(position: Int): Int {
		if (loadingNextPage && position == items.size) {
			return VIEW_TYPE_LOADING_MORE_NEXT_PAGE
		}

		return when (val item = items[position]) {
			is Product -> VIEW_TYPE_PRODUCT
			is SectionHeader -> VIEW_TYPE_SECTION_HEADER
			is AdditionalItemsLoadable -> VIEW_TYPE_MORE_ITEMS_AVAILABLE
			else -> throw IllegalArgumentException("Not able to determine the view type for item at position $position. Item is: $item")
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
		when (viewType) {
			VIEW_TYPE_PRODUCT -> ProductViewHolder.create(layoutInflater, productClickedListener)
			VIEW_TYPE_LOADING_MORE_NEXT_PAGE -> LoadingViewHolder.create(layoutInflater)
			VIEW_TYPE_MORE_ITEMS_AVAILABLE -> MoreItemsViewHolder.create(layoutInflater, this)
			VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder.create(layoutInflater)
			else -> throw IllegalArgumentException("Couldn't create a ViewHolder for viewType  = $viewType")
		}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val item = items[position]
		when (holder) {
			is LoadingViewHolder -> Unit
			is ProductViewHolder -> holder.bind(item as Product)
			is SectionHeaderViewHolder -> holder.onBind(item as SectionHeader)
			is MoreItemsViewHolder -> holder.bind(item as AdditionalItemsLoadable)
			else -> throw IllegalArgumentException("couldn't accept  ViewHolder $holder")
		}
	}

	override fun getItemCount(): Int {
		return items.size +
				if (loadingNextPage) {
					1
				} else {
					0
				}
	}

	override fun loadItemsForCategory(category: String) {
		loadMoreItemsOfCategoryObservable.onNext(category)
	}

	fun loadMoreItemsOfCategoryObservable(): Observable<String> {
		return loadMoreItemsOfCategoryObservable
	}

}