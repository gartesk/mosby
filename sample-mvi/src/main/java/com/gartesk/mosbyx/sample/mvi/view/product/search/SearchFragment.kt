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
package com.gartesk.mosbyx.sample.mvi.view.product.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.gartesk.mosbyx.mvi.MviFragment
import com.gartesk.mosbyx.sample.mvi.R
import com.gartesk.mosbyx.sample.mvi.SampleApplication
import com.gartesk.mosbyx.sample.mvi.businesslogic.interactor.search.SearchViewState
import com.gartesk.mosbyx.sample.mvi.businesslogic.interactor.search.SearchViewState.*
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.FeedItem.Product
import com.gartesk.mosbyx.sample.mvi.view.detail.ProductDetailsActivity
import com.gartesk.mosbyx.sample.mvi.view.product.ProductViewHolder.ProductClickedListener
import com.gartesk.mosbyx.sample.mvi.view.ui.GridSpacingItemDecoration
import com.jakewharton.rxbinding3.widget.queryTextChanges
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchFragment :
	MviFragment<SearchView, SearchPresenter>(),
	SearchView,
	ProductClickedListener {
	private lateinit var searchView: android.widget.SearchView
	private lateinit var container: ViewGroup
	private lateinit var loadingView: View
	private lateinit var errorView: TextView
	private lateinit var recyclerView: RecyclerView
	private lateinit var emptyView: View

	private lateinit var adapter: SearchAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		parent: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_search, parent, false)
		searchView = view.findViewById(R.id.searchView)
		container = view.findViewById(R.id.container)
		loadingView = view.findViewById(R.id.loadingView)
		errorView = view.findViewById(R.id.errorView)
		recyclerView = view.findViewById(R.id.recyclerView)
		emptyView = view.findViewById(R.id.emptyView)

		val spanCount = requireContext().resources.getInteger(R.integer.grid_span_size)
		adapter = SearchAdapter(inflater, this)
		recyclerView.adapter = adapter
		recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
		recyclerView.addItemDecoration(
			GridSpacingItemDecoration(
				spanCount = spanCount,
				spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
				includeEdge = true
			)
		)
		return view
	}

	override fun onProductClicked(product: Product) {
		ProductDetailsActivity.start(requireActivity(), product)
	}

	override fun createPresenter(): SearchPresenter {
		Timber.d("createPresenter")
		return SampleApplication.getDependencyInjection(requireContext()).newSearchPresenter()
	}

	override fun searchIntent(): Observable<String> =
		searchView.queryTextChanges()
			.skip(2) // Because after screen orientation changes query Text will be resubmitted again
			.filter { queryString: CharSequence -> queryString.length > 3 || queryString.isEmpty() }
			.debounce(500, TimeUnit.MILLISECONDS)
			.distinctUntilChanged()
			.map { obj: CharSequence -> obj.toString() }

	override fun render(viewState: SearchViewState) {
		Timber.d("render $viewState")
		when (viewState) {
			is SearchNotStartedYet -> renderSearchNotStarted()
			is Loading -> renderLoading()
			is SearchResult -> renderResult(viewState.result)
			is EmptyResult -> renderEmptyResult()
			is Error -> renderError(viewState.error)
		}
	}

	private fun renderResult(result: List<Product>) {
		TransitionManager.beginDelayedTransition(container)
		recyclerView.visibility = View.VISIBLE
		loadingView.visibility = View.GONE
		emptyView.visibility = View.GONE
		errorView.visibility = View.GONE
		adapter.setProducts(result)
		adapter.notifyDataSetChanged()
	}

	private fun renderSearchNotStarted() {
		TransitionManager.beginDelayedTransition(container)
		recyclerView.visibility = View.GONE
		loadingView.visibility = View.GONE
		errorView.visibility = View.GONE
		emptyView.visibility = View.GONE
	}

	private fun renderLoading() {
		TransitionManager.beginDelayedTransition(container)
		recyclerView.visibility = View.GONE
		loadingView.visibility = View.VISIBLE
		errorView.visibility = View.GONE
		emptyView.visibility = View.GONE
	}

	private fun renderError(error: Throwable) {
		Timber.e(error)
		TransitionManager.beginDelayedTransition(container)
		recyclerView.visibility = View.GONE
		loadingView.visibility = View.GONE
		errorView.visibility = View.VISIBLE
		emptyView.visibility = View.GONE
	}

	private fun renderEmptyResult() {
		TransitionManager.beginDelayedTransition(container)
		recyclerView.visibility = View.GONE
		loadingView.visibility = View.GONE
		errorView.visibility = View.GONE
		emptyView.visibility = View.VISIBLE
	}
}