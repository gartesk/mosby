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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.gartesk.mosbyx.mvi.layout.MviFrameLayout
import com.gartesk.mosbyx.sample.mvi.R
import com.gartesk.mosbyx.sample.mvi.SampleApplication
import io.reactivex.Observable
import timber.log.Timber

class MainMenuLayout(
	context: Context,
	attrs: AttributeSet?
) : MviFrameLayout<MainMenuView, MainMenuPresenter>(context, attrs),
	MainMenuView {

	private val loadingView: View
	private val recyclerView: RecyclerView
	private val errorView: View

	private val adapter: MainMenuAdapter

	init {
		View.inflate(context, R.layout.view_mainmenu, this)
		loadingView = findViewById(R.id.loadingView)
		recyclerView = findViewById(R.id.recyclerView)
		errorView = findViewById(R.id.errorView)
		
		adapter = MainMenuAdapter(LayoutInflater.from(context))
		recyclerView.adapter = adapter
	}

	override fun createPresenter(): MainMenuPresenter {
		Timber.d("Create MainMenuPresenter")
		return SampleApplication.getDependencyInjection(context).mainMenuPresenter
	}

	override fun loadCategoriesIntent(): Observable<Unit> = Observable.just(Unit)

	override fun selectCategoryIntent(): Observable<String> = adapter.selectedItemObservable

	override fun render(menuViewState: MenuViewState) {
		Timber.d("Render $menuViewState")
		TransitionManager.beginDelayedTransition(this)

		when (menuViewState) {
			is MenuViewState.LoadingState -> {
				loadingView.visibility = View.VISIBLE
				recyclerView.visibility = View.GONE
				errorView.visibility = View.GONE
			}

			is MenuViewState.DataState -> {
				adapter.setItems(menuViewState.categories)
				adapter.notifyDataSetChanged()
				loadingView.visibility = View.GONE
				recyclerView.visibility = View.VISIBLE
				errorView.visibility = View.GONE
			}

			is MenuViewState.ErrorState -> {
				loadingView.visibility = View.GONE
				recyclerView.visibility = View.GONE
				errorView.visibility = View.VISIBLE
			}
		}
	}
}