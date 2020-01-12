/*
 * Copyright 2016 Hannes Dorfmann.
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
package com.hannesdorfmann.mosby3.sample.mvi.view.product.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.Product
import com.hannesdorfmann.mosby3.sample.mvi.view.detail.ProductDetailsActivity
import com.hannesdorfmann.mosby3.sample.mvi.view.product.ProductViewHolder.ProductClickedListener
import com.hannesdorfmann.mosby3.sample.mvi.view.ui.GridSpacingItemDecoration
import io.reactivex.Observable
import timber.log.Timber

/**
 * Displays all products of a certain category on the screen
 */
class CategoryFragment :
	MviFragment<CategoryView, CategoryPresenter>(),
	CategoryView,
	ProductClickedListener {

	companion object {
		private const val CATEGORY_NAME = "categoryName"

		fun newInstance(categoryName: String): CategoryFragment =
			CategoryFragment().apply {
				arguments = Bundle().apply { putString(CATEGORY_NAME, categoryName) }
			}
	}

	lateinit var recyclerView: RecyclerView
	lateinit var loadingView: View

	lateinit var errorView: View

	private lateinit var adapter: CategoryAdapter

	override fun onProductClicked(product: Product) {
		ProductDetailsActivity.start(requireActivity(), product)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		parent: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_category, parent, false)
		recyclerView = view.findViewById(R.id.recyclerView)
		loadingView = view.findViewById(R.id.loadingView)
		errorView = view.findViewById(R.id.errorView)

		val spanCount = requireContext().resources.getInteger(R.integer.grid_span_size)
		adapter = CategoryAdapter(inflater, this)
		recyclerView.adapter = adapter
		recyclerView.layoutManager = GridLayoutManager(activity, spanCount)
		recyclerView.addItemDecoration(
			GridSpacingItemDecoration(
				spanCount = spanCount,
				spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing),
				includeEdge = true
			)
		)
		return view
	}

	override fun createPresenter(): CategoryPresenter {
		Timber.d("Create presenter")
		return SampleApplication.getDependencyInjection(requireContext()).newCategoryPresenter()
	}

	override fun loadIntents(): Observable<String> {
		val category = arguments?.getString(CATEGORY_NAME)
			?: throw IllegalStateException("No category name provided")
		return Observable.just(category)
	}

	override fun render(state: CategoryViewState) {
		Timber.d("Render $state")
		when (state) {
			is CategoryViewState.LoadingState -> renderLoading()
			is CategoryViewState.DataState -> renderData(state.products)
			is CategoryViewState.ErrorState -> renderError()
		}
	}

	private fun renderError() {
		TransitionManager.beginDelayedTransition(view as ViewGroup)
		loadingView.visibility = View.GONE
		errorView.visibility = View.VISIBLE
		recyclerView.visibility = View.GONE
	}

	private fun renderLoading() {
		TransitionManager.beginDelayedTransition(view as ViewGroup)
		loadingView.visibility = View.VISIBLE
		errorView.visibility = View.GONE
		recyclerView.visibility = View.GONE
	}

	private fun renderData(products: List<Product>) {
		adapter.setProducts(products)
		adapter.notifyDataSetChanged()
		TransitionManager.beginDelayedTransition(view as ViewGroup)
		loadingView.visibility = View.GONE
		errorView.visibility = View.GONE
		recyclerView.visibility = View.VISIBLE
	}
}