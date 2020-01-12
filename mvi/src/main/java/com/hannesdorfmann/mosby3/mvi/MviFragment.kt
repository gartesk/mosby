/*
 * Copyright 2016 Hannes Dorfmann.
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
 *
 */
package com.hannesdorfmann.mosby3.mvi

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.hannesdorfmann.mosby3.FragmentMviDelegate
import com.hannesdorfmann.mosby3.FragmentMviDelegateImpl
import com.hannesdorfmann.mosby3.MviDelegateCallback
import com.hannesdorfmann.mosby3.mvp.MvpView

/**
 * This abstract class can be used to extend from to implement an Model-View-Intent pattern with
 * this fragment as View and a [MviPresenter] to coordinate the View and the underlying model (business logic)
 *
 * Per default [FragmentMviDelegateImpl] is used with the following lifecycle:
 * The View is attached to the Presenter in [onViewCreated]. So you better instantiate all your
 * UI widgets before that lifecycle callback (typically in [onCreateView]).
 *
 * The View is detached from Presenter in [onDestroyView]
 */
abstract class MviFragment<V : MvpView, P : MviPresenter<V, *>> :
	Fragment(), MvpView, MviDelegateCallback<V, P> {

	private var isRestoringViewState = false

	/**
	 * Get the mvi delegate. This is internally used for creating presenter, attaching and detaching
	 * viewState from presenter.
	 *
	 * **Please note that only one instance of mvi delegate should be used per Fragment instance**.
	 *
	 * Only override this property if you really know what you are doing.
	 *
	 * @return [FragmentMviDelegate]
	 */
	protected val mviDelegate: FragmentMviDelegate<V, P> by lazy {
		FragmentMviDelegateImpl(this, this)
	}

	@CallSuper
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mviDelegate.onCreate(savedInstanceState)
	}

	@CallSuper
	override fun onDestroy() {
		super.onDestroy()
		mviDelegate.onDestroy()
	}

	@CallSuper
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		mviDelegate.onSaveInstanceState(outState)
	}

	@CallSuper
	override fun onPause() {
		super.onPause()
		mviDelegate.onPause()
	}

	@CallSuper
	override fun onResume() {
		super.onResume()
		mviDelegate.onResume()
	}

	@CallSuper
	override fun onStart() {
		super.onStart()
		mviDelegate.onStart()
	}

	@CallSuper
	override fun onStop() {
		super.onStop()
		mviDelegate.onStop()
	}

	@CallSuper
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		mviDelegate.onViewCreated(view, savedInstanceState)
	}

	@CallSuper
	override fun onDestroyView() {
		super.onDestroyView()
		mviDelegate.onDestroyView()
	}

	@CallSuper
	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		mviDelegate.onActivityCreated(savedInstanceState)
	}

	@CallSuper
	@Deprecated("")
	override fun onAttach(activity: Activity) {
		super.onAttach(activity)
		mviDelegate.onAttach(activity)
	}

	@CallSuper
	override fun onAttach(context: Context) {
		super.onAttach(context)
		mviDelegate.onAttach(context)
	}

	@CallSuper
	override fun onDetach() {
		super.onDetach()
		mviDelegate.onDetach()
	}

	@CallSuper
	override fun onAttachFragment(childFragment: Fragment) {
		super.onAttachFragment(childFragment)
		mviDelegate.onAttachFragment(childFragment)
	}

	@CallSuper
	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		mviDelegate.onConfigurationChanged(newConfig)
	}

	/**
	 * Instantiate a presenter instance
	 *
	 * @return The [MviPresenter] for this viewState
	 */
	abstract override fun createPresenter(): P

	override val mvpView: V
		get() = try {
			this as V
		} catch (e: ClassCastException) {
			val msg = "Couldn't cast the View to the corresponding View interface. " +
					"Most likely you forgot to implement your mvi interface in this View."
			Log.e(this.toString(), msg)
			throw RuntimeException(msg, e)
		}

	override fun setRestoringViewState(restoringViewState: Boolean) {
		isRestoringViewState = restoringViewState
	}

	protected fun isRestoringViewState(): Boolean {
		return isRestoringViewState
	}
}