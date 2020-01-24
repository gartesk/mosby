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
package com.gartesk.mosbyx.sample.mvi.view.shoppingcartlabel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.AbsSavedState
import androidx.appcompat.widget.AppCompatButton
import com.gartesk.mosbyx.mvi.delegate.viewgroup.ViewGroupMviDelegate
import com.gartesk.mosbyx.mvi.delegate.viewgroup.ViewGroupMviDelegateCallback
import com.gartesk.mosbyx.mvi.delegate.viewgroup.ViewGroupMviDelegateImpl
import com.gartesk.mosbyx.sample.mvi.R
import com.gartesk.mosbyx.sample.mvi.SampleApplication
import io.reactivex.Observable
import timber.log.Timber

/**
 * A UI widget that displays how many items are in the shopping cart
 */
class ShoppingCartLabel(
	context: Context,
	attrs: AttributeSet?
) : AppCompatButton(context, attrs),
	ShoppingCartLabelView,
	ViewGroupMviDelegateCallback<ShoppingCartLabelView, ShoppingCartLabelPresenter> {

	private val mviDelegate: ViewGroupMviDelegate<ShoppingCartLabelView, ShoppingCartLabelPresenter> =
		ViewGroupMviDelegateImpl(
			this,
			this,
			true
		)

	override val mviView: ShoppingCartLabelView
		get() = this

	override fun createPresenter(): ShoppingCartLabelPresenter {
		Timber.d("create presenter")
		return SampleApplication.getDependencyInjection(context)
			.newShoppingCartLabelPresenter()
	}

	override fun superOnSaveInstanceState(): Parcelable? = super.onSaveInstanceState()

	override fun superOnRestoreInstanceState(state: Parcelable?) {
		super.onRestoreInstanceState(state)
	}

	override fun loadIntent(): Observable<Unit> = Observable.just(Unit)

	override fun render(itemsInShoppingCart: Int) {
		Timber.d("render $itemsInShoppingCart items in shopping cart")
		text = resources.getQuantityString(R.plurals.items, itemsInShoppingCart, itemsInShoppingCart)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		mviDelegate.onAttachedToWindow()
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		mviDelegate.onDetachedFromWindow()
	}

	@SuppressLint("MissingSuperCall")
	override fun onSaveInstanceState(): Parcelable =
		mviDelegate.onSaveInstanceState() ?: AbsSavedState.EMPTY_STATE

	@SuppressLint("MissingSuperCall")
	override fun onRestoreInstanceState(state: Parcelable) {
		mviDelegate.onRestoreInstanceState(state)
	}

	override fun setRestoringViewState(restoringViewState: Boolean) = Unit
}