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
package com.gartesk.mosbyx.mvi.sample.view.product

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gartesk.mosbyx.sample.mvi.R
import com.gartesk.mosbyx.mvi.sample.businesslogic.model.FeedItem.Product
import com.gartesk.mosbyx.mvi.sample.dependencyinjection.DependencyInjection

/**
 * View Holder just to display
 */
class ProductViewHolder private constructor(
	itemView: View,
	clickedListener: ProductClickedListener
) : RecyclerView.ViewHolder(itemView) {

	companion object {
		fun create(inflater: LayoutInflater, listener: ProductClickedListener): ProductViewHolder =
			ProductViewHolder(
				inflater.inflate(R.layout.item_product, null, false),
				listener
			)
	}

	private val image: ImageView = itemView.findViewById(R.id.productImage)
	private val name: TextView = itemView.findViewById(R.id.productName)
	private lateinit var product: Product

	init {
		itemView.setOnClickListener { clickedListener.onProductClicked(product) }
	}

	fun bind(product: Product) {
		this.product = product
		Glide.with(itemView.context)
			.load(DependencyInjection.BASE_IMAGE_URL + product.image)
			.centerCrop()
			.into(image)
		name.text = product.name
	}

	interface ProductClickedListener {
		fun onProductClicked(product: Product)
	}
}