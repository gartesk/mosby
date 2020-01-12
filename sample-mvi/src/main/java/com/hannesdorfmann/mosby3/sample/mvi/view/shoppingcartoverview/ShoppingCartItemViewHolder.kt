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

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.dependencyinjection.DependencyInjection
import java.util.*

class ShoppingCartItemViewHolder private constructor(
	itemView: View,
	private val selectedListener: ItemSelectedListener
) : RecyclerView.ViewHolder(itemView) {

	companion object {
		fun create(inflater: LayoutInflater, selectedListener: ItemSelectedListener): ShoppingCartItemViewHolder =
			ShoppingCartItemViewHolder(
				inflater.inflate(R.layout.item_shopping_cart, null, false),
				selectedListener
			)
	}

	private val selectedDrawable: Drawable =
		ColorDrawable(ContextCompat.getColor(itemView.context, R.color.selected_shopping_cart_item))

	private val image: ImageView = itemView.findViewById(R.id.image)
	private val name: TextView = itemView.findViewById(R.id.name)
	private val price: TextView = itemView.findViewById(R.id.price)

	private lateinit var item: ShoppingCartOverviewItem

	init {
		itemView.setOnClickListener { selectedListener.onItemClicked(item) }
		itemView.setOnLongClickListener { selectedListener.onItemLongPressed(item) }
	}

	fun bind(item: ShoppingCartOverviewItem) {
		this.item = item
		val product = item.product
		Glide.with(itemView.context)
			.load(DependencyInjection.BASE_IMAGE_URL + product.image)
			.centerCrop()
			.into(image)
		name.text = product.name
		price.text = String.format(Locale.US, "$ %.2f", product.price)
		if (item.selected) {
			if (Build.VERSION.SDK_INT >= 23) {
				itemView.foreground = selectedDrawable
			} else {
				itemView.background = selectedDrawable
			}
		} else {
			if (Build.VERSION.SDK_INT >= 23) {
				itemView.foreground = null
			} else {
				itemView.background = null
			}
		}
	}

	interface ItemSelectedListener {
		fun onItemClicked(item: ShoppingCartOverviewItem)
		fun onItemLongPressed(item: ShoppingCartOverviewItem): Boolean
	}
}
