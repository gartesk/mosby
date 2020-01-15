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
package com.gartesk.mosbyx.sample.mvi.view.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.MainMenuItem
import com.gartesk.mosbyx.sample.mvi.view.menu.MainMenuViewHolder.MainMenuSelectionListener
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class MainMenuAdapter(private val layoutInflater: LayoutInflater) :
	RecyclerView.Adapter<MainMenuViewHolder>(), MainMenuSelectionListener {

	private var items: List<MainMenuItem> = emptyList()

	private val selectedItem = PublishSubject.create<String>()

	val selectedItemObservable: Observable<String>
		get() = selectedItem

	fun setItems(items: List<MainMenuItem>) {
		this.items = items
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainMenuViewHolder =
		MainMenuViewHolder.create(layoutInflater, this)

	override fun onBindViewHolder(holder: MainMenuViewHolder, position: Int) {
		holder.bind(items[position])
	}

	override fun getItemCount(): Int = items.size

	override fun onItemSelected(categoryName: String) {
		selectedItem.onNext(categoryName)
	}

}