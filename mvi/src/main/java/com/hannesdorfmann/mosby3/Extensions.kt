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
package com.hannesdorfmann.mosby3

import android.app.Activity
import androidx.fragment.app.Fragment
import java.io.PrintWriter
import java.io.StringWriter

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
 * As of version 1.0 of AndroidX - fragment.isInBackStack() is package private which leads to
 * an IllegalAccessError being thrown when trying to use it.
 * This method is a temporary workaround until the issue is resolved in AndroidX.
 *
 * @return true, if the given Fragment is on the back stack, otherwise false (not on the back
 * stack)
 */
fun Fragment.isInBackStack(): Boolean {
	val writer = StringWriter()
	dump("", null, PrintWriter(writer), null)
	val dump = writer.toString()
	return !dump.contains("mBackStackNesting=0")
}