package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.viewgroup

import android.content.Context
import android.util.AttributeSet
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestView
import com.hannesdorfmann.mosby3.mvi.layout.MviFrameLayout

class TestMviFrameLayout(context: Context, attrs: AttributeSet) :
	MviFrameLayout<LifecycleTestView, LifecycleTestPresenter>(context, attrs), LifecycleTestView {

    val presenter = LifecycleTestPresenter()
    var createPresenterInvocations = 0

	override fun createPresenter(): LifecycleTestPresenter {
		createPresenterInvocations++
		return presenter
	}
}