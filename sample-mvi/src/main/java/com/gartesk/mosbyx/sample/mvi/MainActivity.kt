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
package com.gartesk.mosbyx.sample.mvi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.gartesk.mosbyx.sample.mvi.businesslogic.model.MainMenuItem
import com.gartesk.mosbyx.sample.mvi.view.home.HomeFragment
import com.gartesk.mosbyx.sample.mvi.view.menu.MenuViewState
import com.gartesk.mosbyx.sample.mvi.view.product.category.CategoryFragment
import com.gartesk.mosbyx.sample.mvi.view.product.search.SearchFragment
import com.gartesk.mosbyx.sample.mvi.view.selectedcounttoolbar.SelectedCountToolbar
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.Subject
import timber.log.Timber

class MainActivity : AppCompatActivity() {

	companion object {
		private const val KEY_TOOLBAR_TITLE = "toolbarTitle"
	}

	lateinit var toolbar: Toolbar

	lateinit var drawer: DrawerLayout
	private lateinit var clearSelectionRelay: Subject<Unit>

	private var disposable: Disposable? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		toolbar = findViewById(R.id.toolbar)
		drawer = findViewById(R.id.drawerLayout)

		toolbar.title = "Mosby MVI"
		toolbar.inflateMenu(R.menu.activity_main_toolbar)
		toolbar.setOnMenuItemClickListener {
			supportFragmentManager.beginTransaction()
				.setCustomAnimations(
					android.R.anim.fade_in,
					android.R.anim.fade_out,
					android.R.anim.fade_in,
					android.R.anim.fade_out
				)
				.add(R.id.drawerLayout, SearchFragment())
				.addToBackStack("Search")
				.commit()
			true
		}

		val toggle = ActionBarDrawerToggle(
			this,
			drawer,
			toolbar,
			R.string.navigation_drawer_open,
			R.string.navigation_drawer_close
		)
		drawer.addDrawerListener(toggle)
		toggle.syncState()

		if (savedInstanceState == null) {
			showCategoryItems(MainMenuItem.HOME)
		} else {
			toolbar.title = savedInstanceState.getString(KEY_TOOLBAR_TITLE)
		}

		val dependencyInjection = SampleApplication.getDependencyInjection(this)

		disposable = dependencyInjection.mainMenuPresenter
			.viewStateObservable
			.filter { it is MenuViewState.DataState }
			.cast(MenuViewState.DataState::class.java)
			.map { state: MenuViewState.DataState ->
				findSelectedMenuItem(state)
			}
			.subscribe { showCategoryItems(it) }
		clearSelectionRelay = dependencyInjection.clearSelectionRelay
	}

	override fun onDestroy() {
		super.onDestroy()
		disposable?.dispose()
		Timber.d("------- Destroyed -------")
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Timber.d("Activity onSaveInstanceState()")
		outState.putString(KEY_TOOLBAR_TITLE, toolbar.title.toString())
	}

	private fun findSelectedMenuItem(state: MenuViewState.DataState): String =
		state.categories.find { it.selected }?.name
			?: throw IllegalStateException("No category is selected in Main Menu$state")

	override fun onBackPressed() {
		val selectedCountToolbar = findViewById<SelectedCountToolbar>(R.id.selectedCountToolbar)
		if (!closeDrawerIfOpen()) {
			if (selectedCountToolbar.visibility == View.VISIBLE) {
				clearSelectionRelay.onNext(Unit)
			} else {
				super.onBackPressed()
			}
		}
	}

	private fun closeDrawerIfOpen(): Boolean {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START)
			return true
		}
		return false
	}

	private fun showCategoryItems(categoryName: String) {
		closeDrawerIfOpen()
		val currentCategory = toolbar.title.toString()
		if (currentCategory != categoryName) {
			toolbar.title = categoryName

			val fragment: Fragment =
				if (categoryName == MainMenuItem.HOME) {
					HomeFragment()
				} else {
					CategoryFragment.newInstance(categoryName)
				}

			supportFragmentManager
				.beginTransaction()
				.replace(R.id.fragmentContainer, fragment)
				.commit()
		}
	}
}