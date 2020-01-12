package com.hannesdorfmann.mosby3.mvi.integrationtest.lifecycle.viewgroup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R

class ViewGroupFinishOnCreateContainerActivity : AppCompatActivity() {

	val layout: ViewGroupFinishOnCreateLayout
		get() = findViewById(R.id.mviLayout)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_view_group_finish_on_create_container)
		finish()
	}
}