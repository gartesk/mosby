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
package com.gartesk.mosbyx

import android.app.Activity
import androidx.fragment.app.Fragment

/**
 * Determines whether or not a Presenter Instance should be kept
 *
 * @param keepPresenterInstance true, if the delegate has enabled keep
 */
internal fun Activity.shouldRetainPresenterInstance(keepPresenterInstance: Boolean): Boolean =
	keepPresenterInstance && (isChangingConfigurations || !isFinishing)

/**
 * Checks whether or not a given fragment is on the backstack of the fragment manager (could also
 * be on top of the backstack and hence visible)
 *
 * This is a hack since in AndroidX Fragment::isInBackStack is not accessible
 *
 * @return true, if the given Fragment is on the back stack, otherwise false (not on the back
 * stack)
 */
fun Fragment.isInBackStack(): Boolean {
	return try {
		val backStackNestingField = Fragment::class.java
			.getDeclaredField("mBackStackNesting")
		backStackNestingField.isAccessible = true
		val backStackNesting = backStackNestingField.getInt(this)
		backStackNesting > 0
	} catch (e: NoSuchFieldException) {
		throw RuntimeException(e)
	} catch (e: IllegalAccessException) {
		throw RuntimeException(e)
	}
}