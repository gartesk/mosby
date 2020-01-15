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
package com.hannesdorfmann.mosby3.sample.mvi.view.shoppingcartoverview

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.sample.mvi.R
import com.hannesdorfmann.mosby3.sample.mvi.SampleApplication
import com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model.FeedItem.Product
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * This class doesn't necessarily has to be a fragment. It's just a fragment because I want to
 * demonstrate that mosby works with fragments in xml layouts too.
 */
class ShoppingCartOverviewFragment :
	MviFragment<ShoppingCartOverviewView, ShoppingCartOverviewPresenter>(),
	ShoppingCartOverviewView {

	private lateinit var adapter: ShoppingCartOverviewAdapter
	private val removeRelay = PublishSubject.create<Product>()
	private lateinit var recyclerView: RecyclerView

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_shopping_cart, container, false)
		adapter = ShoppingCartOverviewAdapter(requireActivity())
		recyclerView = view.findViewById(R.id.shoppingCartRecyclerView)
		recyclerView.adapter = adapter
		recyclerView.layoutManager = LinearLayoutManager(activity)
		setUpItemTouchHelper()
		return view
	}

	override fun createPresenter(): ShoppingCartOverviewPresenter {
		Timber.d("Create Presenter")
		return SampleApplication.getDependencyInjection(requireContext()).shoppingCartPresenter
	}

	override fun loadItemsIntent(): Observable<Unit> = Observable.just(Unit)

	override fun selectItemsIntent(): Observable<List<Product>> = adapter.selectedItemsObservable()

	private fun setUpItemTouchHelper() {
		// Borrowed from https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/blob/master/app/src/main/java/net/nemanjakovacevic/recyclerviewswipetodelete/MainActivity.java
		val simpleItemTouchCallback =
			object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
				// we want to cache these and not allocate anything repeatedly in the onChildDraw method
				var background: Drawable? = null
				var xMark: Drawable? = null
				var xMarkMargin = 0
				var initiated = false

				private fun init() {
					background = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.delete_background))
					xMark = ContextCompat.getDrawable(requireContext(), R.drawable.ic_remove)
					xMarkMargin = activity!!.resources.getDimension(R.dimen.ic_clear_margin).toInt()
					initiated = true
				}

				// not important, we don't want drag & drop
				override fun onMove(
					recyclerView: RecyclerView,
					viewHolder: RecyclerView.ViewHolder,
					target: RecyclerView.ViewHolder
				): Boolean = false

				override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
					val swipedPosition = viewHolder.adapterPosition
					val productAt = adapter.getProductAt(swipedPosition)
					removeRelay.onNext(productAt)
				}

				override fun onChildDraw(
					canvas: Canvas,
					recyclerView: RecyclerView,
					viewHolder: RecyclerView.ViewHolder,
					dX: Float,
					dY: Float,
					actionState: Int,
					currentlyActive: Boolean
				) {
					val itemView = viewHolder.itemView
					// not sure why, but this method gets called for viewholders that are already swiped away
					if (viewHolder.adapterPosition == -1) {
						return
					}
					if (!initiated) {
						init()
					}
					// draw red background
					background?.apply {
						setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
						draw(canvas)
					}
					// draw x mark
					val itemHeight = itemView.bottom - itemView.top
					val intrinsicWidth = xMark?.intrinsicWidth ?: 0
					val intrinsicHeight = xMark?.intrinsicWidth ?: 0
					val xMarkLeft = itemView.right - xMarkMargin - intrinsicWidth
					val xMarkRight = itemView.right - xMarkMargin
					val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
					val xMarkBottom = xMarkTop + intrinsicHeight
					xMark?.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)
					// xMark.draw(c);
					super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, currentlyActive)
				}
			}
		val mItemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
		mItemTouchHelper.attachToRecyclerView(recyclerView)
	}

	override fun removeItemIntent(): Observable<Product> =
		removeRelay.delay(500, TimeUnit.MILLISECONDS)

	override fun render(itemsInShoppingCart: List<ShoppingCartOverviewItem>) {
		Timber.d("Render $itemsInShoppingCart")
		adapter.setItems(itemsInShoppingCart)
	}
}