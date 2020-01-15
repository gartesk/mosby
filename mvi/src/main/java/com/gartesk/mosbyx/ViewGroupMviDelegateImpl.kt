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
import android.app.Application
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View

import com.gartesk.mosbyx.mvi.MviPresenter
import com.gartesk.mosbyx.mvp.MvpView
import java.util.UUID

/**
 * The default implementation of [ViewGroupMviDelegate]
 *
 * @see ViewGroupMviDelegate
 */
class ViewGroupMviDelegateImpl<V : MvpView, P : MviPresenter<V, *>>(
	view: View,
	private val delegateCallback: ViewGroupMviDelegateCallback<V, P>,
	private val keepPresenterDuringScreenOrientationChange: Boolean
) : ViewGroupMviDelegate<V, P>, Application.ActivityLifecycleCallbacks {

	// TODO allow custom save state hook in

	companion object {
		var DEBUG = false
		private const val DEBUG_TAG = "ViewGroupMviDelegateImp"
	}

	private var mosbyViewId: String? = null
	private lateinit var presenter: P

	private val inEditMode: Boolean = view.isInEditMode
	private val activity: Activity? =
		if (!inEditMode) {
			PresenterManager.getActivity(delegateCallback.getContext())
				.apply { application.registerActivityLifecycleCallbacks(this@ViewGroupMviDelegateImpl) }
		} else {
			null
		}

	private var checkedActivityFinishing = false
	private var presenterDetached = false
	private var presenterDestroyed = false

	/**
	 * Generates the unique (mosby internal) viewState id and calls [MviDelegateCallback.createPresenter]
	 * to create a new presenter instance
	 *
	 * @return The new created presenter instance
	 */
	private fun createViewIdAndCreatePresenter(): P {

		val presenter = delegateCallback.createPresenter()
		if (keepPresenterDuringScreenOrientationChange) {
			val context = delegateCallback.getContext()
			val generatedMosbyViewId = UUID.randomUUID().toString()
			mosbyViewId = generatedMosbyViewId
			PresenterManager.putPresenter(PresenterManager.getActivity(context), generatedMosbyViewId, presenter)
		}
		return presenter
	}

	override fun onAttachedToWindow() {
		if (inEditMode) {
			return
		}

		requireNotNull(activity) {
			"Activity is null with inEditMode == false. Something went wrong internally"
		}

		var viewStateWillBeRestored = false

		val currentMosbyViewId = mosbyViewId
		if (currentMosbyViewId == null) {
			// No presenter available,
			// Activity is starting for the first time (or keepPresenterInstance == false)
			presenter = createViewIdAndCreatePresenter()
			if (DEBUG) {
				Log.d(DEBUG_TAG, "New Presenter instance created: $presenter")
			}
		} else {
			val storedPresenter = PresenterManager.getPresenter<P>(activity, currentMosbyViewId)
			if (storedPresenter == null) {
				// Process death,
				// hence no presenter with the given viewState id stored, although we have a viewState id
				presenter = createViewIdAndCreatePresenter()
				if (DEBUG) {
					Log.d(DEBUG_TAG,
						"No Presenter instance found in cache, although MosbyView ID present. " +
								"This was caused by process death, therefore new Presenter " +
								"instance created: $presenter")
				}
			} else {
				presenter = storedPresenter
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

		presenter.attachView(view)

		if (viewStateWillBeRestored) {
			delegateCallback.setRestoringViewState(false)
		}

		if (DEBUG) {
			Log.d(DEBUG_TAG, "MvpView attached to Presenter. MvpView: $view. Presenter: $presenter")
		}
	}

	override fun onDetachedFromWindow() {
		if (inEditMode) {
			return
		}

		requireNotNull(activity) {
			"Activity is null with inEditMode == false. Something went wrong internally"
		}

		detachPresenterIfNotDoneYet()

		if (!checkedActivityFinishing) {
			val destroyPermanently =
				!activity.shouldRetainPresenterInstance(keepPresenterDuringScreenOrientationChange)

			if (destroyPermanently) {
				destroyPresenterIfNotDoneYet()
			} else if (!activity.isFinishing) {
				// View removed manually from screen
				destroyPresenterIfNotDoneYet()
			}
		} // else --> see onActivityDestroyed()
	}

	/**
	 * Must be called from [View.onSaveInstanceState]
	 */
	override fun onSaveInstanceState(): Parcelable? {
		if (inEditMode) {
			return null
		}

		val superState = delegateCallback.superOnSaveInstanceState()

		return if (keepPresenterDuringScreenOrientationChange && superState != null) {
			MosbySavedState(superState, mosbyViewId)
		} else {
			superState
		}
	}

	/**
	 * Restore the data from SavedState
	 *
	 * @param state The state to read data from
	 */
	private fun restoreSavedState(state: MosbySavedState) {
		mosbyViewId = state.mosbyViewId
	}

	/**
	 * Must be called from [View.onRestoreInstanceState]
	 */
	override fun onRestoreInstanceState(state: Parcelable) {
		if (inEditMode) {
			return
		}

		if (state !is MosbySavedState) {
			delegateCallback.superOnRestoreInstanceState(state)
			return
		}

		restoreSavedState(state)
		delegateCallback.superOnRestoreInstanceState(state.superState)
	}

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) = Unit

	override fun onActivityStarted(activity: Activity) = Unit

	override fun onActivityResumed(activity: Activity) = Unit

	override fun onActivityPaused(activity: Activity) = Unit

	override fun onActivityStopped(activity: Activity) = Unit

	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

	override fun onActivityDestroyed(activity: Activity) {
		if (activity === this.activity) {
			// The hosting activity of this view has been destroyed, so time to destroy the presenter too?

			activity.application.unregisterActivityLifecycleCallbacks(this)
			checkedActivityFinishing = true

			val destroyedPermanently =
				!activity.shouldRetainPresenterInstance(keepPresenterDuringScreenOrientationChange)

			if (destroyedPermanently) {
				// Whole activity will be destroyed
				detachPresenterIfNotDoneYet()
				destroyPresenterIfNotDoneYet()
			}
		}
	}

	private fun destroyPresenterIfNotDoneYet() {
		if (!presenterDestroyed) {
			presenter.destroy()
			presenterDestroyed = true
			if (DEBUG) {
				Log.d(DEBUG_TAG, "Presenter destroyed: $presenter")
			}

			mosbyViewId?.let {
				requireNotNull(activity) {
					"Activity is null with mosbyViewId present. Something went wrong internally"
				}
				// mosbyViewId == null if keepPresenterDuringScreenOrientationChange == false
				PresenterManager.remove(activity, it)
			}
			mosbyViewId = null
		}
	}

	private fun detachPresenterIfNotDoneYet() {
		if (!presenterDetached) {
			presenter.detachView()
			presenterDetached = true
			if (DEBUG) {
				Log.d(DEBUG_TAG, "View ${delegateCallback.mvpView} detached from Presenter $presenter")
			}
		}
	}
}
