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
package com.hannesdorfmann.mosby3.sample.mvi.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.Product
import com.hannesdorfmann.mosby3.sample.mvi.view.detail.ProductDetailsActivity
import com.hannesdorfmann.mosby3.sample.mvi.view.product.ProductViewHolder.ProductClickedListener
import com.jakewharton.rxbinding3.recyclerview.scrollStateChanges
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import io.reactivex.Observable
import timber.log.Timber

class HomeFragment :
	MviFragment<HomeView, HomePresenter>(),
	HomeView,
	ProductClickedListener {

	lateinit var swipeRefreshLayout: SwipeRefreshLayout
	lateinit var recyclerView: RecyclerView
	lateinit var loadingView: View
	lateinit var errorView: TextView

	private lateinit var adapter: HomeAdapter
	private lateinit var layoutManager: GridLayoutManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_home, container, false)
		swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
		recyclerView = view.findViewById(R.id.recyclerView)
		loadingView = view.findViewById(R.id.loadingView)
		errorView = view.findViewById(R.id.errorView)

		adapter = HomeAdapter(inflater, this)
		val spanSize = requireContext().resources.getInteger(R.integer.grid_span_size)
		layoutManager = GridLayoutManager(activity, spanSize)
		layoutManager.spanSizeLookup = object : SpanSizeLookup() {
			override fun getSpanSize(position: Int): Int {
				val viewType = adapter.getItemViewType(position)

				return if (viewType == HomeAdapter.VIEW_TYPE_LOADING_MORE_NEXT_PAGE
					|| viewType == HomeAdapter.VIEW_TYPE_SECTION_HEADER
				) {
					spanSize
				} else {
					1
				}
			}
		}

		recyclerView.adapter = adapter
		recyclerView.layoutManager = layoutManager
		return view
	}

	override fun createPresenter(): HomePresenter {
		Timber.d("createPresenter")
		return SampleApplication.getDependencyInjection(requireContext()).newHomePresenter()
	}

	override fun onProductClicked(product: Product) {
		ProductDetailsActivity.start(requireActivity(), product)
	}

	override fun loadFirstPageIntent(): Observable<Unit> {
		return Observable.just(Unit)
	}

	override fun loadNextPageIntent(): Observable<Unit> =
		recyclerView.scrollStateChanges()
			.filter { !adapter.loadingNextPage }
			.filter { it == RecyclerView.SCROLL_STATE_IDLE }
			.filter { layoutManager.findLastCompletelyVisibleItemPosition() == adapter.items.size - 1 }
			.map { Unit }

	override fun pullToRefreshIntent(): Observable<Unit> =
		swipeRefreshLayout.refreshes()

	override fun loadAllProductsFromCategoryIntent(): Observable<String> =
		adapter.loadMoreItemsOfCategoryObservable()

	override fun render(viewState: HomeViewState) {
		Timber.d("render $viewState")
		when {
			!viewState.loadingFirstPage && viewState.firstPageError == null -> renderShowData(viewState)
			viewState.loadingFirstPage -> renderFirstPageLoading()
			viewState.firstPageError != null -> renderFirstPageError()
		}
	}

	private fun renderShowData(state: HomeViewState) {
		val container = view as ViewGroup
		TransitionManager.beginDelayedTransition(container)

		loadingView.visibility = View.GONE
		errorView.visibility = View.GONE
		swipeRefreshLayout.visibility = View.VISIBLE

		val changed = adapter.setLoadingNextPage(state.loadingNextPage)
		if (changed && state.loadingNextPage) {
			recyclerView.smoothScrollToPosition(adapter.itemCount)
		}

		adapter.setItems(state.data)

		val pullToRefreshFinished = swipeRefreshLayout.isRefreshing
				&& !state.loadingPullToRefresh
				&& state.pullToRefreshError == null
		if (pullToRefreshFinished) {
			recyclerView.smoothScrollToPosition(0)
		}

		swipeRefreshLayout.isRefreshing = state.loadingPullToRefresh

		if (state.nextPageError != null) {
			Snackbar.make(container, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
		}
		if (state.pullToRefreshError != null) {
			Snackbar.make(container, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
		}
	}

	private fun renderFirstPageLoading() {
		TransitionManager.beginDelayedTransition(view as ViewGroup)
		loadingView.visibility = View.VISIBLE
		errorView.visibility = View.GONE
		swipeRefreshLayout.visibility = View.GONE
	}

	private fun renderFirstPageError() {
		TransitionManager.beginDelayedTransition(view as ViewGroup)
		loadingView.visibility = View.GONE
		swipeRefreshLayout.visibility = View.GONE
		errorView.visibility = View.VISIBLE
	}
}