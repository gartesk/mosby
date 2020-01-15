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

import android.os.Parcelable
import android.view.View
import android.widget.FrameLayout
import com.gartesk.mosbyx.mvp.MvpPresenter
import com.gartesk.mosbyx.mvp.MvpView

/**
 * The mvp delegate used for everything that derives from [View] like [FrameLayout]
 * etc.
 *
 * The following methods must be called from the corresponding View lifecycle method:
 *
 *  * [onAttachedToWindow]
 *  * [onDetachedFromWindow]
 *  * [onSaveInstanceState]
 *  * [onRestoreInstanceState]
 */
interface ViewGroupMviDelegate<V : MvpView, P : MvpPresenter<V>> {

	/**
	 * Must be called from [View.onAttachedToWindow]
	 */
	fun onAttachedToWindow()

	/**
	 * Must be called from [View.onDetachedFromWindow]
	 */
	fun onDetachedFromWindow()

	/**
	 * Must be called from [View.onRestoreInstanceState]
	 *
	 * @param state The parcelable state
	 */
	fun onRestoreInstanceState(state: Parcelable)

	/**
	 * Save the instance state
	 *
	 * @return The state with all the saved data
	 */
	fun onSaveInstanceState(): Parcelable?
}
