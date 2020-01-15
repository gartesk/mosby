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
package com.hannesdorfmann.mosby3.sample.mvi.view.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.interactor.details.ProductDetailsViewState
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.Product
import com.hannesdorfmann.mosby3.sample.mvi.dependencyinjection.DependencyInjection
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import timber.log.Timber
import java.util.*

class ProductDetailsActivity :
	MviActivity<ProductDetailsView, ProductDetailsPresenter>(),
	ProductDetailsView {

	companion object {
		const val KEY_PRODUCT_ID = "productId"

		fun start(activity: Activity, product: Product) {
			val intent = Intent(activity, ProductDetailsActivity::class.java).apply {
				putExtra(KEY_PRODUCT_ID, product.id)
			}
			activity.startActivity(intent)
		}
	}

	private var product: Product? = null
	private var productInShoppingCart = false
	private lateinit var fabClickObservable: Observable<Unit>

	lateinit var errorView: View
	lateinit var loadingView: View
	lateinit var detailsView: View
	lateinit var price: TextView
	lateinit var description: TextView
	lateinit var fab: FloatingActionButton
	lateinit var toolbar: Toolbar
	lateinit var backdrop: ImageView
	lateinit var rootView: ViewGroup
	lateinit var collapsingToolbarLayout: CollapsingToolbarLayout

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_product_detail)
		errorView = findViewById(R.id.errorView)
		loadingView = findViewById(R.id.loadingView)
		detailsView = findViewById(R.id.detailsView)
		price = findViewById(R.id.price)
		description = findViewById(R.id.description)
		fab = findViewById(R.id.fab)
		toolbar = findViewById(R.id.toolbar)
		backdrop = findViewById(R.id.backdrop)
		rootView = findViewById(R.id.root)
		collapsingToolbarLayout = findViewById(R.id.collapsingToolbar)
		setSupportActionBar(toolbar)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		fabClickObservable = fab.clicks().share()
	}

	override fun createPresenter(): ProductDetailsPresenter {
		Timber.d("Create presenter")
		return SampleApplication.getDependencyInjection(this).newProductDetailsPresenter()
	}

	override fun loadDetailsIntent(): Observable<Int> =
		Observable.just(intent.getIntExtra(KEY_PRODUCT_ID, 0))

	override fun addToShoppingCartIntent(): Observable<Product> =
		fabClickObservable
			.filter { product != null }
			.filter { !productInShoppingCart }
			.map { product }

	override fun removeFromShoppingCartIntent(): Observable<Product> =
		fabClickObservable
			.filter { product != null }
			.filter { productInShoppingCart }
			.map { product }

	override fun render(state: ProductDetailsViewState) {
		Timber.d("render $state")
		when (state) {
			is ProductDetailsViewState.LoadingState -> renderLoading()
			is ProductDetailsViewState.DataState -> renderData(state)
			is ProductDetailsViewState.ErrorState -> renderError()
		}
	}

	private fun renderError() {
		TransitionManager.beginDelayedTransition(rootView)
		errorView.visibility = View.VISIBLE
		loadingView.visibility = View.GONE
		detailsView.visibility = View.GONE
	}

	private fun renderData(viewState: ProductDetailsViewState.DataState) {
		TransitionManager.beginDelayedTransition(rootView)
		errorView.visibility = View.GONE
		loadingView.visibility = View.GONE
		detailsView.visibility = View.VISIBLE

		productInShoppingCart = viewState.detail.inShoppingCart

		if (productInShoppingCart) {
			fab.setImageResource(R.drawable.ic_in_shopping_cart)
		} else {
			fab.setImageResource(R.drawable.ic_add_shopping_cart)
		}

		product = viewState.detail.product.also {
			price.text = "Price: $${String.format(Locale.US, "%.2f", it.price)}"
			description.text = it.description
			toolbar.title = it.name
			collapsingToolbarLayout.title = it.name
			Glide.with(this)
				.load(DependencyInjection.BASE_IMAGE_URL + it.image)
				.centerCrop()
				.into(backdrop)
		}
	}

	private fun renderLoading() {
		TransitionManager.beginDelayedTransition(rootView)
		errorView.visibility = View.GONE
		loadingView.visibility = View.VISIBLE
		detailsView.visibility = View.GONE
	}
}