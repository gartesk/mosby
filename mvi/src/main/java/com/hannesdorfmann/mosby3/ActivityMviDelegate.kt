/*
 * Copyright 2015 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import androidx.fragment.app.FragmentActivity
import com.hannesdorfmann.mosby3.mvi.MviPresenter
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.hannesdorfmann.mosby3.mvp.MvpView

/**
 * A delegate for Activities to attach them to mosby mvp.
 *
 * The following methods must be invoked from the corresponding Activities lifecycle methods:
 *
 *  * [onCreate]
 *  * [onDestroy]
 *  * [onPause]
 *  * [onResume]
 *  * [onStart]
 *  * [onStop]
 *  * [onRestart]
 *  * [onContentChanged]
 *  * [onSaveInstanceState]
 *  * [onPostCreate]
 *
 * @param [V] The type of [MvpView]
 * @param [P] The type of [MvpPresenter]
 */
interface ActivityMviDelegate<V : MvpView, P : MviPresenter<V, *>> {

	/**
	 * This method must be called from [Activity.onCreate].
	 * This method internally creates the presenter and attaches the viewState to it.
	 */
	fun onCreate(bundle: Bundle?)

	/**
	 * This method must be called from [Activity.onDestroy]}.
	 * This method internally detaches the viewState from presenter
	 */
	fun onDestroy()

	/**
	 * This method must be called from [Activity.onPause]
	 */
	fun onPause()

	/**
	 * This method must be called from [Activity.onResume]
	 */
	fun onResume()

	/**
	 * This method must be called from [Activity.onStart]
	 */
	fun onStart()

	/**
	 * This method must be called from [Activity.onStop]
	 */
	fun onStop()

	/**
	 * This method must be called from [Activity.onRestart]
	 */
	fun onRestart()

	/**
	 * This method must be called from [Activity.onContentChanged]
	 */
	fun onContentChanged()

	/**
	 * This method must be called from [Activity.onSaveInstanceState]
	 */
	fun onSaveInstanceState(outState: Bundle)

	/**
	 * This method must be called from [Activity.onPostCreate]
	 */
	fun onPostCreate(savedInstanceState: Bundle?)

	/**
	 * This method must be called from [FragmentActivity.onRetainCustomNonConfigurationInstance]
	 *
	 * @return Don't forget to return the value returned by this delegate method
	 */
	fun onRetainCustomNonConfigurationInstance(): Any?
}
