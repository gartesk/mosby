package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.fragment

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R

class MviFinishOnStartContainerActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_lifecycle)
		if (savedInstanceState == null) {
			val fragment = MviFinishOnStartFragment()
			supportFragmentManager
				.beginTransaction()
				.replace(R.id.container, fragment)
				.commitNow()
		}
	}
}