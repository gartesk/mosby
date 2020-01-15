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
package com.gartesk.mosbyx.sample.mvi.view.selectedcounttoolbar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.AbsSavedState
import android.view.View
import android.widget.Toolbar
import com.gartesk.mosbyx.ViewGroupMviDelegate
import com.gartesk.mosbyx.ViewGroupMviDelegateCallback
import com.gartesk.mosbyx.ViewGroupMviDelegateImpl
import com.gartesk.mosbyx.sample.mvi.R
import com.gartesk.mosbyx.sample.mvi.SampleApplication
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class SelectedCountToolbar(
	context: Context,
	attrs: AttributeSet?
) : Toolbar(context, attrs),
	SelectedCountToolbarView,
	ViewGroupMviDelegateCallback<SelectedCountToolbarView, SelectedCountToolbarPresenter> {

	private val mviDelegate: ViewGroupMviDelegate<SelectedCountToolbarView, SelectedCountToolbarPresenter> =
		ViewGroupMviDelegateImpl(this, this, true)
	private val clearSelectionIntent = PublishSubject.create<Unit>()
	private val deleteSelectedItemsIntent = PublishSubject.create<Unit>()

	init {
		setNavigationOnClickListener { clearSelectionIntent.onNext(Unit) }
		setNavigationIcon(R.drawable.ic_back_selection_count_toolbar)
		inflateMenu(R.menu.shopping_cart_toolbar)
		setOnMenuItemClickListener {
			deleteSelectedItemsIntent.onNext(Unit)
			true
		}
	}

	override fun clearSelectionIntent(): Observable<Unit> = clearSelectionIntent

	override fun deleteSelectedItemsIntent(): Observable<Unit> = deleteSelectedItemsIntent

	override val mvpView: SelectedCountToolbarView
		get() = this

	override fun createPresenter(): SelectedCountToolbarPresenter {
		Timber.d("create presenter")
		return SampleApplication.getDependencyInjection(context)
			.newSelectedCountToolbarPresenter()
	}

	override fun render(selectedCount: Int) {
		Timber.d("render $selectedCount selected items")
		if (selectedCount == 0) {
			if (visibility == View.VISIBLE) {
				animate().alpha(0f)
					.withEndAction { visibility = View.GONE }
					.start()
			} else {
				visibility = View.GONE
			}
		} else {
			title = resources.getQuantityString(
				R.plurals.items,
				selectedCount,
				selectedCount
			)
			if (visibility != View.VISIBLE) {
				animate().alpha(1f)
					.withStartAction { visibility = View.VISIBLE }
					.start()
			} else {
				visibility = View.VISIBLE
			}
		}
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
	public override fun onSaveInstanceState(): Parcelable =
		mviDelegate.onSaveInstanceState() ?: AbsSavedState.EMPTY_STATE

	@SuppressLint("MissingSuperCall")
	public override fun onRestoreInstanceState(state: Parcelable) {
		mviDelegate.onRestoreInstanceState(state)
	}

	override fun superOnSaveInstanceState(): Parcelable? = super.onSaveInstanceState()

	override fun superOnRestoreInstanceState(state: Parcelable?) {
		super.onRestoreInstanceState(state)
	}

	override fun setRestoringViewState(restoringViewState: Boolean) = Unit
}