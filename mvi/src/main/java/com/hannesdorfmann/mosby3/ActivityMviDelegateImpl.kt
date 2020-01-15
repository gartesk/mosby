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
package com.hannesdorfmann.mosby3

import android.app.Activity
import android.os.Bundle
import android.util.Log

import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView
import java.util.UUID

/**
 * The concrete implementation of [ActivityMviDelegate].
 * This delegate creates the Presenter and attaches the View to the presenter in [Activity.onStart].
 * The view is detached from presenter in [Activity.onStop]
 *
 * @param [V] The type of [MvpView]
 * @param [P] The type of [MvpPresenter]
 * @see ActivityMviDelegate
 */
class ActivityMviDelegateImpl<V : MvpView, P : MviPresenter<V, *>>
/**
 * Creates a new delegate
 *
 * @param activity The activity
 * @param delegateCallback The delegate callback
 * @param keepPresenterInstance true, if the presenter instance should be kept through screen
 * orientation changes, false if not (a new presenter instance will be created every time you
 * rotate your device)
 */
constructor(
	private val activity: Activity,
	private val delegateCallback: MviDelegateCallback<V, P>,
	private val keepPresenterInstance: Boolean = true
) : ActivityMviDelegate<V, P> {

	companion object {
		var DEBUG = false
		private const val DEBUG_TAG = "ActivityMviDelegateImpl"
		private const val KEY_MOSBY_VIEW_ID = "com.hannesdorfmann.mosby3.activity.mvi.id"
	}
	private var mosbyViewId: String? = null

	private var presenter: P? = null

	override fun onCreate(bundle: Bundle?) {
		if (keepPresenterInstance && bundle != null) {
			mosbyViewId = bundle.getString(KEY_MOSBY_VIEW_ID)
		}

		if (DEBUG) {
			Log.d(DEBUG_TAG, "MosbyView ID = $mosbyViewId for MvpView: ${delegateCallback.mvpView}")
		}
	}

	override fun onStart() {
		var viewStateWillBeRestored = false

		val currentMosbyViewId = mosbyViewId
		if (currentMosbyViewId == null) {
			// No presenter available,
			// Activity is starting for the first time (or keepPresenterInstance == false)
			presenter = createViewIdAndCreatePresenter()
			if (DEBUG) {
				Log.d(DEBUG_TAG, "New Presenter instance created: $presenter " +
						"for ${delegateCallback.mvpView}")
			}
		} else {
			presenter = PresenterManager.getPresenter<P>(activity, currentMosbyViewId)
			if (presenter == null) {
				// Process death,
				// hence no presenter with the given viewState id stored, although we have a viewState id
				presenter = createViewIdAndCreatePresenter()
				if (DEBUG) {
					Log.d(DEBUG_TAG, "No Presenter instance found in cache, although " +
								"MosbyView ID present. This was caused by process death, therefore " +
								"new Presenter instance created: $presenter")
				}
			} else {
				viewStateWillBeRestored = true
				if (DEBUG) {
					Log.d(DEBUG_TAG, "Presenter instance reused from internal cache: $presenter")
				}
			}
		}

		// presenter is ready, so attach viewState
		val view = delegateCallback.mvpView

		if (viewStateWillBeRestored) {
			delegateCallback.setRestoringViewState(true)
		}

		presenter?.attachView(view)

		if (viewStateWillBeRestored) {
			delegateCallback.setRestoringViewState(false)
		}

		if (DEBUG) {
			Log.d(DEBUG_TAG, "MvpView attached to Presenter. MvpView: $view. Presenter: $presenter")
		}
	}

	/**
	 * Generates the unique (mosby internal) view id and calls [MviDelegateCallback.createPresenter]
	 * to create a new presenter instance
	 *
	 * @return The new created presenter instance
	 */
	private fun createViewIdAndCreatePresenter(): P {

		val presenter = delegateCallback.createPresenter()
		if (keepPresenterInstance) {
			val generatedMosbyViewId = UUID.randomUUID().toString()
			mosbyViewId = generatedMosbyViewId
			PresenterManager.putPresenter(activity, generatedMosbyViewId, presenter)
		}
		return presenter
	}

	override fun onSaveInstanceState(outState: Bundle) {
		if (keepPresenterInstance) {
			outState.putString(KEY_MOSBY_VIEW_ID, mosbyViewId)
			if (DEBUG) {
				Log.d(DEBUG_TAG, "Saving MosbyViewId into Bundle. ViewId: $mosbyViewId")
			}
		}
	}

	override fun onStop() {
		presenter?.detachView()

		if (DEBUG) {
			Log.d(DEBUG_TAG, "Detached MvpView from Presenter. " +
						"MvpView: ${delegateCallback.mvpView}. Presenter: $presenter")
		}
	}

	override fun onDestroy() {
		presenter?.let { notNullPresenter ->
			// Presenter is only null if Activity.finish() called before Activity.onStart() has been reached
			val retainPresenterInstance = activity.shouldRetainPresenterInstance(keepPresenterInstance)
			if (!retainPresenterInstance) {
				notNullPresenter.destroy()
				mosbyViewId?.let {
					// mosbyViewId == null if keepPresenterInstance == false
					PresenterManager.remove(activity, it)
				}
				Log.d(DEBUG_TAG, "Destroying Presenter permanently $notNullPresenter")
			}

		}
		presenter = null
	}

	override fun onPostCreate(savedInstanceState: Bundle?) {}

	override fun onPause() = Unit

	override fun onResume() = Unit

	override fun onRestart() = Unit

	override fun onContentChanged() = Unit

	override fun onRetainCustomNonConfigurationInstance(): Any? = null
}


