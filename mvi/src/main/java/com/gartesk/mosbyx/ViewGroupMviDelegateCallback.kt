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

import android.content.Context
import android.os.Parcelable
import com.gartesk.mosbyx.mvi.MviPresenter
import com.gartesk.mosbyx.mvi.MviView

/**
 * An enhanced version of [MviDelegateCallback] that adds support for [android.view.ViewGroup]
 */
interface ViewGroupMviDelegateCallback<V : MviView, P : MviPresenter<V, *>> :
	MviDelegateCallback<V, P> {

	/**
	 * Get the context
	 */
	fun getContext(): Context

	/**
	 * This method must call super.onSaveInstanceState() within any view
	 */
	fun superOnSaveInstanceState(): Parcelable?

	/**
	 * This method must call super.onRestoreInstanceState(state)
	 *
	 * @param state The parcelable containing the state
	 */
	fun superOnRestoreInstanceState(state: Parcelable?)
}
