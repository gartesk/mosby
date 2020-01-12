package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.viewgroup

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R

class MviViewGroupContainerActivity : AppCompatActivity() {

	companion object {

		private lateinit var currentInstance: Activity

		val mviViewGroup: TestMviFrameLayout
			get() = currentInstance.findViewById(R.id.testFrameLayout)

		fun pressBackButton() {
			currentInstance.runOnUiThread { currentInstance.onBackPressed() }
		}

		fun removeMviViewGroup() {
			currentInstance.runOnUiThread {
				val rootView = currentInstance.findViewById<ViewGroup>(R.id.rootView)
				rootView.removeAllViews()
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		currentInstance = this
		setContentView(R.layout.activity_viewgroup_mvi)
	}
}