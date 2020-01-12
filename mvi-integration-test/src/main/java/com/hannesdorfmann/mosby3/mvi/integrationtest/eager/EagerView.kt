package com.hannesdorfmann.mosby3.mvi.integrationtest.eager

import com.hannesdorfmann.mosby3.mvp.MvpView
import io.reactivex.Observable

interface EagerView : MvpView {
	fun intent1(): Observable<String>
	fun intent2(): Observable<String>
	fun render(state: String)
}