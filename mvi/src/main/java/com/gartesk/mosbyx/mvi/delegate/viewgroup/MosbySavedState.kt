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
package com.gartesk.mosbyx.mvi.delegate.viewgroup

import android.os.Parcel
import android.os.Parcelable

import androidx.customview.view.AbsSavedState

/**
 * The SavedState implementation to store the view's internal id to
 */
class MosbySavedState : AbsSavedState {

	companion object {

		@JvmField
		val CREATOR: Parcelable.Creator<MosbySavedState> =
			object : Parcelable.ClassLoaderCreator<MosbySavedState> {

				override fun createFromParcel(source: Parcel): MosbySavedState =
					MosbySavedState(
						source,
						MosbySavedState::class.java.classLoader
					)

				override fun createFromParcel(source: Parcel, loader: ClassLoader?): MosbySavedState {
					val actualLoader = loader ?: MosbySavedState::class.java.classLoader
					return MosbySavedState(
						source,
						actualLoader
					)
				}

				override fun newArray(size: Int): Array<MosbySavedState?> = arrayOfNulls(size)
			}
	}

	var mosbyViewId: String? = null
		private set

	constructor(superState: Parcelable, mosbyViewId: String?) : super(superState) {
		this.mosbyViewId = mosbyViewId
	}

	private constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
		this.mosbyViewId = source.readString()
	}

	override fun writeToParcel(out: Parcel, flags: Int) {
		super.writeToParcel(out, flags)
		out.writeString(mosbyViewId)
	}
}
