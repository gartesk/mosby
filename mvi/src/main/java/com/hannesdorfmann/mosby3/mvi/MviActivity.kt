package com.hannesdorfmann.mosby3.mvi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.mosby3.ActivityMviDelegate
import com.hannesdorfmann.mosby3.ActivityMviDelegateImpl
import com.hannesdorfmann.mosby3.MviDelegateCallback
import com.hannesdorfmann.mosby3.mvp.MvpView

/**
 * This abstract class can be used to extend from to implement an Model-View-Intent pattern with
 * this activity as View and a [MviPresenter] to coordinate the View and the underlying model (business logic).
 *
 * Per default [ActivityMviDelegateImpl] is used which means the View is attached to the
 * presenter in [onStart]. You better initialize all your UI components before that, typically in
 * [onCreate].
 *
 * The view is detached from presenter in [onStop]
 */
abstract class MviActivity<V : MvpView, P : MviPresenter<V, *>> :
	AppCompatActivity(), MvpView, MviDelegateCallback<V, P> {

	private var isRestoringViewState = false

	/**
	 * Get the mvi delegate. This is internally used for creating presenter, attaching and detaching
	 * viewState from presenter.
	 *
	 * **Please note that only one instance of mvi delegate should be used per Activity instance**
	 *
	 * Only override this property if you really know what you are doing.
	 *
	 * @return [ActivityMviDelegate]
	 */
	protected val mviDelegate: ActivityMviDelegate<V, P> by lazy {
		ActivityMviDelegateImpl(this, this)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mviDelegate.onCreate(savedInstanceState)
	}

	override fun onDestroy() {
		super.onDestroy()
		mviDelegate.onDestroy()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		mviDelegate.onSaveInstanceState(outState)
	}

	override fun onPause() {
		super.onPause()
		mviDelegate.onPause()
	}

	override fun onResume() {
		super.onResume()
		mviDelegate.onResume()
	}

	override fun onStart() {
		super.onStart()
		mviDelegate.onStart()
	}

	override fun onStop() {
		super.onStop()
		mviDelegate.onStop()
	}

	override fun onRestart() {
		super.onRestart()
		mviDelegate.onRestart()
	}

	override fun onContentChanged() {
		super.onContentChanged()
		mviDelegate.onContentChanged()
	}

	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		mviDelegate.onPostCreate(savedInstanceState)
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

	override fun onRetainCustomNonConfigurationInstance(): Any? {
		return mviDelegate.onRetainCustomNonConfigurationInstance()
	}

	override fun setRestoringViewState(restoringViewState: Boolean) {
		isRestoringViewState = restoringViewState
	}

	protected fun isRestoringViewState(): Boolean {
		return isRestoringViewState
	}
}