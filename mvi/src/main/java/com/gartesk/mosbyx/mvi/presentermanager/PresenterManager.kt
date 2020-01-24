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
package com.gartesk.mosbyx.mvi.presentermanager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.annotation.MainThread
import com.gartesk.mosbyx.mvi.MviPresenter

import com.gartesk.mosbyx.mvi.MviView
import java.util.UUID

/**
 * A internal class responsible to save internal presenter instances during screen orientation
 * changes and reattach the presenter afterwards.
 *
 * The idea is that each MVI View (like a Activity, Fragment, ViewGroup) will get a unique view id.
 * This view id is used to store the presenter and viewstate in it. After screen orientation changes
 * we can reuse the presenter and viewstate by querying for the given view id (must be saved in
 * view's state somehow).
 */
object PresenterManager {

	var DEBUG = false
	const val DEBUG_TAG = "PresenterManager"
	internal const val KEY_ACTIVITY_ID = "com.gartesk.mosbyx.MosbyPresenterManagerActivityId"

	internal val activityIdMap = mutableMapOf<Activity, String>()
	private val activityScopedCacheMap = mutableMapOf<String, ActivityScopedCache>()

	internal val activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks =
		object : Application.ActivityLifecycleCallbacks {
			override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
				if (savedInstanceState != null) {
					val activityId = savedInstanceState.getString(KEY_ACTIVITY_ID)
					if (activityId != null) {
						// After a screen orientation change we map the newly created Activity to the same
						// Activity ID as the previous activity has had (before screen orientation change)
						activityIdMap[activity] = activityId
					}
				}
			}

			override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
				// Save the activityId into bundle so that the other
				val activityId = activityIdMap[activity]
				if (activityId != null) {
					outState.putString(KEY_ACTIVITY_ID, activityId)
				}
			}

			override fun onActivityStarted(activity: Activity) = Unit

			override fun onActivityResumed(activity: Activity) = Unit

			override fun onActivityPaused(activity: Activity) = Unit

			override fun onActivityStopped(activity: Activity) = Unit

			override fun onActivityDestroyed(activity: Activity) {
				if (!activity.isChangingConfigurations) {
					// Activity will be destroyed permanently, so reset the cache
					val activityId = activityIdMap[activity]
					if (activityId != null) {
						val scopedCache = activityScopedCacheMap[activityId]
						if (scopedCache != null) {
							scopedCache.clear()
							activityScopedCacheMap.remove(activityId)
						}

						// No Activity Scoped cache available, so unregister
						if (activityScopedCacheMap.isEmpty()) {
							// All MosbyX related activities are destroyed, so we can remove the activity lifecycle listener
							activity.application.unregisterActivityLifecycleCallbacks(this)
							if (DEBUG) {
								Log.d(DEBUG_TAG, "Unregistering ActivityLifecycleCallbacks")
							}
						}
					}
				}
				activityIdMap.remove(activity)
			}
		}

	/**
	 * Get an already existing [ActivityScopedCache] or creates a new one if not existing yet
	 *
	 * @param activity The Activitiy for which you want to get the activity scope for
	 * @return The [ActivityScopedCache] for the given Activity
	 */
	@MainThread
	internal fun getOrCreateActivityScopedCache(activity: Activity): ActivityScopedCache {
		var activityId = activityIdMap[activity]
		if (activityId == null) {
			// Activity not registered yet
			activityId = UUID.randomUUID().toString()
			activityIdMap[activity] = activityId

			if (activityIdMap.size == 1) {
				// Added the an Activity for the first time so register Activity LifecycleListener
				activity.application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
				if (DEBUG) {
					Log.d(DEBUG_TAG, "Registering ActivityLifecycleCallbacks")
				}
			}
		}

		var activityScopedCache = activityScopedCacheMap[activityId]
		if (activityScopedCache == null) {
			activityScopedCache =
				ActivityScopedCache()
			activityScopedCacheMap[activityId] = activityScopedCache
		}

		return activityScopedCache
	}

	/**
	 * Get the [ActivityScopedCache] for the given Activity or `null` if no [ActivityScopedCache]
	 * exists for the given Activity
	 *
	 * @param activity The activity
	 * @return The [ActivityScopedCache] or null
	 * @see [getOrCreateActivityScopedCache]
	 */
	@MainThread
	internal fun getActivityScope(activity: Activity): ActivityScopedCache? {
		val activityId = activityIdMap[activity] ?: return null
		return activityScopedCacheMap[activityId]
	}

	/**
	 * Get the presenter for the View with the given (MosbyX-internal) view Id or `null`
	 * if no presenter for the given view (via view id) exists.
	 *
	 * @param activity The Activity (used for scoping)
	 * @param viewId The MosbyX internal View Id (unique among all [MviView]
	 * @param [P] The Presenter type
	 * @return The Presenter or `null`
	 */
	fun <P> getPresenter(activity: Activity, viewId: String): P? {
		val scopedCache =
			getActivityScope(
				activity
			)
		return if (scopedCache == null) {
			null
		} else {
			scopedCache.getPresenter<Any>(viewId) as P?
		}
	}

	/**
	 * Get the ViewState for the View with the given (MosbyX-internal)
	 * view Id or `null` if no viewstate for the given view exists.
	 *
	 * @param activity The Activity (used for scoping)
	 * @param viewId The MosbyX internal View Id (unique among all [MviView]
	 * @param [VS] The type of the ViewState type
	 * @return The Presenter or `null`
	 */
	fun <VS> getViewState(activity: Activity, viewId: String): VS? {
		val scopedCache =
			getActivityScope(
				activity
			)
		return if (scopedCache == null) {
			null
		} else {
			scopedCache.getViewState<Any>(viewId) as VS?
		}
	}

	/**
	 * Get the Activity of a context. This is typically used to determine the hosting activity of a [View]
	 *
	 * @param context The context
	 * @return The Activity or throws an Exception if Activity couldnt be determined
	 */
	fun getActivity(context: Context): Activity {
		if (context is Activity) {
			return context
		}

		var baseContext = context
		while (baseContext is ContextWrapper) {
			if (baseContext is Activity) {
				return baseContext
			}
			baseContext = baseContext.baseContext
		}
		throw IllegalStateException("Could not find the surrounding Activity")
	}

	/**
	 * Clears the internal (static) state. Used for testing.
	 */
	internal fun reset() {
		activityIdMap.clear()
		for (scopedCache in activityScopedCacheMap.values) {
			scopedCache.clear()
		}

		activityScopedCacheMap.clear()
	}

	/**
	 * Puts the presenter into the internal cache
	 *
	 * @param activity The parent activity
	 * @param viewId the view id (MosbyX internal)
	 * @param presenter the presenter
	 */
	fun putPresenter(activity: Activity, viewId: String, presenter: MviPresenter<out MviView, *>) {
		val scopedCache =
			getOrCreateActivityScopedCache(
				activity
			)
		scopedCache.putPresenter(viewId, presenter)
	}

	/**
	 * Puts the viewstate into the internal cache
	 *
	 * @param activity The parent activity
	 * @param viewId the view id (MosbyX internal)
	 * @param viewState the presenter
	 */
	fun putViewState(activity: Activity, viewId: String, viewState: Any) {
		val scopedCache =
			getOrCreateActivityScopedCache(
				activity
			)
		scopedCache.putViewState(viewId, viewState)
	}

	/**
	 * Removes the Presenter (and ViewState) for the given View. Does nothing if no Presenter is
	 * stored internally with the given viewId
	 *
	 * @param activity The activity
	 * @param viewId The MosbyX internal view id
	 */
	fun remove(activity: Activity, viewId: String) {
		val activityScope =
			getActivityScope(
				activity
			)
		activityScope?.remove(viewId)
	}
}



