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
package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model

/**
 * Just an empty interface representing a list of items that can be displayed on screen
 */
sealed class FeedItem {

	/**
	 * This is a indicator that also some more items are available that could be loaded
	 */
	data class AdditionalItemsLoadable(
		val moreItemsCount: Int,
		val categoryName: String,
		val loading: Boolean,
		val loadingError: Throwable?
	) : FeedItem()

	/**
	 * This is a pojo model class representing a Product
	 */
	data class Product(
		val id: Int,
		val image: String?,
		val name: String,
		val category: String,
		val description: String,
		val price: Double
	) : FeedItem()

	/**
	 * A section header used to group elemens
	 */
	data class SectionHeader(val name: String) : FeedItem()
}

