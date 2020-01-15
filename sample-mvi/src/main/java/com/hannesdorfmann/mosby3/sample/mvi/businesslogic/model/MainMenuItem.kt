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
package com.hannesdorfmann.mosby3.sample.mvi.businesslogic.model

/**
 * Represents a main menu item that can be selected
 */
data class MainMenuItem(val name: String, val selected: Boolean) {

	companion object {
		/**
		 * Preserved "category" name for the menu item that triggers to the "home" screen
		 */
		const val HOME = "Home"
	}
}