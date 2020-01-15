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
package com.hannesdorfmann.mosby3.sample.mvi.view.home.viewholder

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.SectionHeader

class SectionHeaderViewHolder private constructor(itemView: View) :
	RecyclerView.ViewHolder(itemView) {

	companion object {
		fun create(layoutInflater: LayoutInflater): SectionHeaderViewHolder =
			SectionHeaderViewHolder(layoutInflater.inflate(R.layout.item_section_header, null, false))
	}

	private val sectionName: TextView = itemView.findViewById(R.id.sectionName)

	fun onBind(item: SectionHeader) {
		sectionName.text = item.name
	}
}