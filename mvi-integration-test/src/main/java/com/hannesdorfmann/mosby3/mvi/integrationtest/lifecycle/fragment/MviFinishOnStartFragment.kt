package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby3.mvi.MviFragment
import com.hannesdorfmann.mosby3.mvi.integrationtest.R
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestPresenter
import com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.LifecycleTestView

class MviFinishOnStartFragment :
	MviFragment<LifecycleTestView, LifecycleTestPresenter>(), LifecycleTestView {

	companion object {
		lateinit var presenter: LifecycleTestPresenter
		var presenterCreatedCount = 0
	}

	override fun createPresenter(): LifecycleTestPresenter {
		presenter = LifecycleTestPresenter()
		presenterCreatedCount++
		return presenter
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View = inflater.inflate(R.layout.fragment_mvi, container, false)

	override fun onStart() {
		super.onStart()
		requireActivity().finish()
	}
}