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
package com.gartesk.mosbyx.mvi.sample.view.shoppingcartlabel

import com.gartesk.mosbyx.mvi.MviView
import io.reactivex.Observable

/**
 * A View that displays the number of items in the shopping cart
 */
interface ShoppingCartLabelView : MviView {

	/**
	 * Intent to load the data
	 */
	fun loadIntent(): Observable<Unit>

	fun render(itemsInShoppingCart: Int)
}