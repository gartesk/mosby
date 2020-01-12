package com.hannesdorfmann.mosby3.mvi.integrationtest.eager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.hannesdorfmann.mosby3.mvi.MviActivity
import com.hannesdorfmann.mosby3.mvi.integrationtest.R
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject

class EagerViewActivity : MviActivity<EagerView, EagerPresenter>(), EagerView {

    var renderedStrings = ReplaySubject.create<String>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_eager_view)
	}

	override fun intent1(): Observable<String> =
		Observable.just("Intent 1").startWith("Before Intent 1")

	override fun intent2(): Observable<String> = Observable.just("Intent 2")

	override fun render(state: String) {
		findViewById<TextView>(R.id.text).text = state
		Log.d("Render", state)
		renderedStrings.onNext(state)
	}

	override fun createPresenter(): EagerPresenter = EagerPresenter()
}