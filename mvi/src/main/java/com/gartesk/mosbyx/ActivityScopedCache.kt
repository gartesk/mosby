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
package com.gartesk.mosbyx

import com.gartesk.mosbyx.mvi.MviPresenter
import com.gartesk.mosbyx.mvi.MviView

/**
 * This class basically represents a Map for View Id to the Presenter / ViewState.
 * One instance of this class is also associated by [PresenterManager] to one Activity (kept
 * across screen orientation changes)
 */
internal class ActivityScopedCache {

	private val presenterMap = mutableMapOf<String, PresenterHolder>()

	fun clear() {
		/**
		 * TODO: can this check if there are still Presenters in the internal cache that must be detached?
		 * Maybe post() / postDelayed() on the MainThreadLooper()
		 */
		presenterMap.clear()
	}

	/**
	 * Get the Presenter for a given [MviView] if exists or `null`
	 *
	 * @param viewId The MosbyX internal view id
	 * @param <P> The type tof the [MviPresenter]
	 * @return The Presenter for the given view id or `null`
	 */
	fun <P> getPresenter(viewId: String): P? {
		val holder = presenterMap[viewId]
		return if (holder == null) {
			null
		} else {
			holder.presenter as P?
		}
	}

	/**
	 * Get the ViewState for a given [MviView] if exists or `null`
	 *
	 * @param viewId The MosbyX internal view id
	 * @param <VS> The type of the view state
	 * @return The ViewState for the given view id or `null`
	 */
	fun <VS> getViewState(viewId: String): VS? {
		val holder = presenterMap[viewId]
		return if (holder == null) {
			null
		} else {
			holder.viewState as VS?
		}
	}

	/**
	 * Put the presenter in the internal cache
	 *
	 * @param viewId The MosbyX internal View id of the [MviView] which the presenter is associated to.
	 * @param presenter The Presenter
	 */
	fun putPresenter(viewId: String, presenter: MviPresenter<out MviView, *>) {
		val presenterHolder = presenterMap[viewId]
		if (presenterHolder == null) {
			presenterMap[viewId] = PresenterHolder(presenter = presenter)
		} else {
			presenterMap[viewId] = presenterHolder.copy(presenter = presenter)
		}
	}


	/**
	 * Put the viewstate in the internal cache
	 *
	 * @param viewId The MosbyX internal View id of the [MviView] which the presenter is associated to.
	 * @param viewState The Viewstate
	 */
	fun putViewState(viewId: String, viewState: Any) {
		val presenterHolder = presenterMap[viewId]
		if (presenterHolder == null) {
			presenterMap[viewId] = PresenterHolder(viewState = viewState)
		} else {
			presenterMap[viewId] = presenterHolder.copy(viewState = viewState)
		}
	}

	/**
	 * Removes the Presenter (and ViewState) from the internal storage
	 *
	 * @param viewId The MosbyX internal view id
	 */
	fun remove(viewId: String) {
		presenterMap.remove(viewId)
	}

	/**
	 * Internal config change Cache entry
	 */
	internal data class PresenterHolder(
		val presenter: MviPresenter<*, *>? = null,
		// workaround: didn't want to introduce dependency to viewstate module
		val viewState: Any? = null
	)
}
