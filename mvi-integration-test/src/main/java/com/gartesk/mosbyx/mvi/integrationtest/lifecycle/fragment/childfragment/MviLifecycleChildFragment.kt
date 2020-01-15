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
package com.gartesk.mosbyx.mvi.integrationtest.lifecycle.fragment.childfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gartesk.mosbyx.mvi.MviFragment
import com.gartesk.mosbyx.mvi.integrationtest.R
import com.gartesk.mosbyx.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.gartesk.mosbyx.mvi.integrationtest.lifecycle.LifecycleTestView

class MviLifecycleChildFragment :
	MviFragment<LifecycleTestView, LifecycleTestPresenter>(), LifecycleTestView {

	companion object {
		var createPresenterInvocations = 0
	}

	lateinit var presenter: LifecycleTestPresenter

	override fun createPresenter(): LifecycleTestPresenter {
		createPresenterInvocations++
		presenter = LifecycleTestPresenter()
		return presenter
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = inflater.inflate(R.layout.fragment_mvi, container, false)
}