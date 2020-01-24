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

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

import com.gartesk.mosbyx.mvi.MviPresenter
import com.gartesk.mosbyx.mvi.MviView
import java.util.UUID

/**
 * The default implementation of [FragmentMviDelegate]
 *
 * The View is attached to the Presenter in [Fragment.onStart].
 * So you better instantiate all your UI widgets before that lifecycle callback (typically in
 * [Fragment.onStart]. The View is detached from Presenter in [Fragment.onStop].
 * [MviPresenter.destroy] is called from [ ][Fragment.onDestroy] if Fragment will be destroyed permanently.
 *
 * @param [V] The type of [MviView]
 * @param [P] The type of [MviPresenter]
 * @see FragmentMviDelegate
 *
 */
class FragmentMviDelegateImpl<V : MviView, P : MviPresenter<V, *>> @JvmOverloads constructor(
	private var delegateCallback: MviDelegateCallback<V, P>,
	private var fragment: Fragment,
	private val keepPresenterDuringScreenOrientationChange: Boolean = true,
	private val keepPresenterOnBackStack: Boolean = true
) : FragmentMviDelegate<V, P> {

	companion object {

		var DEBUG = false
		private const val DEBUG_TAG = "FragmentMviDelegateImpl"
		private const val KEY_MOSBY_VIEW_ID = "com.gartesk.mosbyx.fragment.mvi.id"
	}

	private var mosbyViewId: String? = null
	private var onViewCreatedCalled = false
	private lateinit var presenter: P
	private var viewStateWillBeRestored: Boolean = false

	private val activity: Activity
		get() = fragment.requireActivity()

	init {
		require(keepPresenterDuringScreenOrientationChange || !keepPresenterOnBackStack) {
			("It is not possible to keep the presenter on backstack, but NOT keep presenter "
					+ "through screen orientation changes. Keep presenter on backstack also "
					+ "requires keep presenter through screen orientation changes to be enabled")
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		if ((keepPresenterDuringScreenOrientationChange || keepPresenterOnBackStack) && savedInstanceState != null) {
			mosbyViewId = savedInstanceState.getString(KEY_MOSBY_VIEW_ID)
		}

		if (DEBUG) {
			Log.d(DEBUG_TAG, "MosbyView ID = $mosbyViewId for MviView: ${delegateCallback.mviView}")
		}

		val currentMosbyViewId = mosbyViewId
		if (currentMosbyViewId == null) {
			// No presenter available,
			// Activity is starting for the first time (or keepPresenterDuringScreenOrientationChange == false)
			presenter = createViewIdAndCreatePresenter()
			viewStateWillBeRestored = false
			if (DEBUG) {
				Log.d(DEBUG_TAG, "New Presenter instance created: $presenter")
			}
		} else {
			val storedPresenter = PresenterManager.getPresenter<P>(activity, currentMosbyViewId)
			if (storedPresenter == null) {
				// Process death,
				// hence no presenter with the given viewState id stored, although we have a viewState id
				presenter = createViewIdAndCreatePresenter()
				viewStateWillBeRestored = false
				if (DEBUG) {
					Log.d(DEBUG_TAG, "No Presenter instance found in cache, although " +
								"MosbyView ID present. This was caused by process death, " +
								"therefore new Presenter instance created: $presenter")
				}
			} else {
				presenter = storedPresenter
				viewStateWillBeRestored = true
				if (DEBUG) {
					Log.d(DEBUG_TAG, "Presenter instance reused from internal cache: $presenter")
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		onViewCreatedCalled = true
	}

	override fun onStart() {

		// presenter is ready, so attach viewState
		val view = delegateCallback.mviView

		if (viewStateWillBeRestored) {
			delegateCallback.setRestoringViewState(true)
		}

		presenter.attachView(view)

		if (viewStateWillBeRestored) {
			delegateCallback.setRestoringViewState(false)
		}

		if (DEBUG) {
			Log.d(DEBUG_TAG, "MviView attached to Presenter. MviView: $view. Presenter: $presenter")
		}
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {

		check(onViewCreatedCalled) {
			"It seems that onCreateView() has never been called " +
					"(or has returned null). This means that your fragment is headless (no UI). " +
					"That is not allowed because it doesn't make sense to use MosbyX with a " +
					"Fragment without View."
		}
	}

	private fun retainPresenterInstance(
		keepPresenterOnBackStack: Boolean,
		activity: Activity,
		fragment: Fragment
	): Boolean =
		when {
			activity.isChangingConfigurations -> keepPresenterDuringScreenOrientationChange
			activity.isFinishing -> false
			keepPresenterOnBackStack && fragment.isInBackStack() -> true
			else -> !fragment.isRemoving
		}

	override fun onDestroyView() {
		onViewCreatedCalled = false
	}

	override fun onStop() {
		presenter.detachView()
		viewStateWillBeRestored = true // used after screen orientation if retaining Fragment

		if (DEBUG) {
			Log.d(DEBUG_TAG, "Detached MviView from Presenter. " +
						"MviView: ${delegateCallback.mviView}. Presenter: $presenter")
		}
	}

	override fun onDestroy() {
		val activity = activity
		val retainPresenterInstance =
			retainPresenterInstance(keepPresenterOnBackStack, activity, fragment)

		if (!retainPresenterInstance) {
			presenter.destroy()
			mosbyViewId?.let {
				// mosbyViewId == null if keepPresenterDuringScreenOrientationChange == false
				PresenterManager.remove(activity, it)
			}
			if (DEBUG) {
				Log.d(DEBUG_TAG, "Presenter destroyed")
			}
		} else if (DEBUG) {
			Log.d(DEBUG_TAG, "Retaining presenter instance: $retainPresenterInstance $presenter")
		}
	}

	override fun onPause() = Unit

	override fun onResume() = Unit

	/**
	 * Generates the unique (MosbyX internal) viewState id and calls [MviDelegateCallback.createPresenter]
	 * to create a new presenter instance
	 *
	 * @return The new created presenter instance
	 */
	private fun createViewIdAndCreatePresenter(): P {
		val presenter = delegateCallback.createPresenter()
		if (keepPresenterDuringScreenOrientationChange || keepPresenterOnBackStack) {
			val generatedMosbyViewId = UUID.randomUUID().toString()
			mosbyViewId = generatedMosbyViewId
			PresenterManager.putPresenter(activity, generatedMosbyViewId, presenter)
		}
		return presenter
	}

	override fun onSaveInstanceState(outState: Bundle) {
		if (keepPresenterDuringScreenOrientationChange || keepPresenterOnBackStack) {
			outState.putString(KEY_MOSBY_VIEW_ID, mosbyViewId)

			retainPresenterInstance(keepPresenterOnBackStack, activity, fragment)
			if (DEBUG) {
				Log.d(DEBUG_TAG, "Saving MosbyViewId into Bundle. ViewId: $mosbyViewId")
			}
		}
	}

	override fun onAttach(activity: Activity) = Unit

	override fun onDetach() = Unit

	override fun onAttach(context: Context) = Unit

	override fun onAttachFragment(childFragment: Fragment) = Unit

	override fun onConfigurationChanged(newConfig: Configuration) = Unit
}
