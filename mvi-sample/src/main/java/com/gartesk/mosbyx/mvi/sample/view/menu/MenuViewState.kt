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
package com.gartesk.mosbyx.mvi.sample.view.menu

import com.gartesk.mosbyx.mvi.sample.businesslogic.model.MainMenuItem

/**
 * The View state for the Menu
 */
sealed class MenuViewState {

	/**
	 * Loads the list of all menu items
	 */
	object LoadingState : MenuViewState()

	/**
	 * Ane error has occurred while loading the data
	 */
	data class ErrorState(val error: Throwable) : MenuViewState()

	/**
	 * Data has been loaded successfully and can now be displayed
	 */
	data class DataState(val categories: List<MainMenuItem>) : MenuViewState()
}