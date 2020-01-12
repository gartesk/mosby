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
package com.hannesdorfmann.mosby3.sample.mvi.view.home.viewholder

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.AdditionalItemsLoadable

class MoreItemsViewHolder private constructor(
	itemView: View,
	listener: LoadItemsClickListener
) : RecyclerView.ViewHolder(itemView) {
	
	companion object {
		fun create(layoutInflater: LayoutInflater, clickListener: LoadItemsClickListener): MoreItemsViewHolder =
			MoreItemsViewHolder(
				layoutInflater.inflate(R.layout.item_more_available, null, false), 
				clickListener
			)
	}

	private val moreItemsCount: TextView = itemView.findViewById(R.id.moreItemsCount)
	private val loadingView: View = itemView.findViewById(R.id.loadingView)
	private val loadMoreButton: View = itemView.findViewById(R.id.loadMoreButtton)
	private val errorRetry: Button = itemView.findViewById(R.id.errorRetryButton)

	private lateinit var currentItem: AdditionalItemsLoadable

	init {
		itemView.setOnClickListener { listener.loadItemsForCategory(currentItem.categoryName) }
		errorRetry.setOnClickListener { listener.loadItemsForCategory(currentItem.categoryName) }
		loadMoreButton.setOnClickListener { listener.loadItemsForCategory(currentItem.categoryName) }
	}
	
	fun bind(item: AdditionalItemsLoadable) {
		currentItem = item
		when {
			item.loading -> {
				moreItemsCount.visibility = View.GONE
				loadMoreButton.visibility = View.GONE
				loadingView.visibility = View.VISIBLE
				errorRetry.visibility = View.GONE
				itemView.isClickable = false
			}

			item.loadingError != null -> {
				moreItemsCount.visibility = View.GONE
				loadMoreButton.visibility = View.GONE
				loadingView.visibility = View.GONE
				errorRetry.visibility = View.VISIBLE
				itemView.isClickable = true
			}

			else -> {
				moreItemsCount.text = itemView.context.getString(R.string.action_more_items_format, item.moreItemsCount)
				moreItemsCount.visibility = View.VISIBLE
				loadMoreButton.visibility = View.VISIBLE
				loadingView.visibility = View.GONE
				errorRetry.visibility = View.GONE
				itemView.isClickable = true
			}
		}
	}

	interface LoadItemsClickListener {
		fun loadItemsForCategory(category: String)
	}
}