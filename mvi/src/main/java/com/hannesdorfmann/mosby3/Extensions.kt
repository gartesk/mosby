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