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
package com.gartesk.mosbyx.mvi.sample.view.product.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gartesk.mosbyx.mvi.sample.businesslogic.model.FeedItem.Product
import com.gartesk.mosbyx.mvi.sample.view.product.ProductViewHolder
import com.gartesk.mosbyx.mvi.sample.view.product.ProductViewHolder.ProductClickedListener

/**
 * Adapter display search results
 */
class SearchAdapter(
	private val inflater: LayoutInflater,
	private val productClickedListener: ProductClickedListener
) : RecyclerView.Adapter<ProductViewHolder>() {

	private var products: List<Product> = emptyList()

	fun setProducts(products: List<Product>) {
		this.products = products
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder =
		ProductViewHolder.create(inflater, productClickedListener)

	override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
		holder.bind(products[position])
	}

	override fun getItemCount(): Int = products.size

}