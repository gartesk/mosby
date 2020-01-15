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
package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.viewgroup

import android.content.Context
import android.util.AttributeSet
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestView
import com.hannesdorfmann.mosby3.mvi.layout.MviFrameLayout

class ViewGroupFinishOnCreateLayout(context: Context, attrs: AttributeSet) :
	MviFrameLayout<LifecycleTestView, LifecycleTestPresenter>(context, attrs), LifecycleTestView {

	var presenter: LifecycleTestPresenter? = null
	var createPresenterInvocations = 0

	override fun createPresenter(): LifecycleTestPresenter {
		createPresenterInvocations++
		val createdPresenter = LifecycleTestPresenter()
		presenter = createdPresenter
		return createdPresenter
	}
}