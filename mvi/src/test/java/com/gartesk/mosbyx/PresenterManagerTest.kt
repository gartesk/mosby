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
import android.app.Application
import android.os.Bundle
import com.gartesk.mosbyx.mvi.MviPresenter
import com.gartesk.mosbyx.mvi.MviView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(Activity::class)
class PresenterManagerTest {

	@Before
	fun clear() {
		PresenterManager.reset()
	}

	@Test
	fun returnsSameScopedCacheAndRegisterLifecycleListener() {
		val activity = mock(Activity::class.java)
		val application = mock(Application::class.java)
		`when`(activity.application).thenReturn(application)

		val scopedCache1 = PresenterManager.getOrCreateActivityScopedCache(activity)
		val scopedCache2 = PresenterManager.getOrCreateActivityScopedCache(activity)

		assertTrue(scopedCache1 == scopedCache2)

		verify(application, times(1))
			.registerActivityLifecycleCallbacks(PresenterManager.activityLifecycleCallbacks)
	}

	@Test
	fun removesLifecycleListener() {
		val activity = mock(Activity::class.java)
		val application = mock(Application::class.java)
		`when`(activity.application).thenReturn(application)
		`when`(activity.isChangingConfigurations).thenReturn(false)

		PresenterManager.getOrCreateActivityScopedCache(activity)

		verify(application, times(1))
			.registerActivityLifecycleCallbacks(PresenterManager.activityLifecycleCallbacks)

		PresenterManager.activityLifecycleCallbacks.onActivityDestroyed(activity)

		verify(application, times(1))
			.unregisterActivityLifecycleCallbacks(PresenterManager.activityLifecycleCallbacks)
	}

	@Test
	fun saveActivityIdAndRestoreFromBundle() {
		val bundle1 = mock(Bundle::class.java)
		val portraitActivity1 = mock(Activity::class.java)
		val landscapeActivity1 = mock(Activity::class.java)
		val application = MockApplication()

		`when`(portraitActivity1.application).thenReturn(application)
		`when`(landscapeActivity1.application).thenReturn(application)
		`when`(portraitActivity1.isChangingConfigurations).thenReturn(true)
		`when`(landscapeActivity1.isChangingConfigurations).thenReturn(false)

		// This one also registers for lifecycle events
		val scopedCache1 = PresenterManager.getOrCreateActivityScopedCache(portraitActivity1)
		val activityId = PresenterManager.activityIdMap[portraitActivity1]
		`when`(bundle1.getString(PresenterManager.KEY_ACTIVITY_ID)).thenReturn(activityId)

		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)

		PresenterManager.activityLifecycleCallbacks.onActivityPaused(portraitActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivityStopped(portraitActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivitySaveInstanceState(portraitActivity1, bundle1)
		PresenterManager.activityLifecycleCallbacks.onActivityDestroyed(portraitActivity1)

		verify(bundle1).putString(PresenterManager.KEY_ACTIVITY_ID, activityId)
		// Don't unregister, because it's a screen orientation change
		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)

		// Simulate orientation change
		PresenterManager.activityLifecycleCallbacks.onActivityCreated(landscapeActivity1, bundle1)
		PresenterManager.activityLifecycleCallbacks.onActivityStarted(landscapeActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivityResumed(landscapeActivity1)

		val scopedCache2 = PresenterManager.getOrCreateActivityScopedCache(landscapeActivity1)
		assertTrue(scopedCache1 == scopedCache2)

		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)

		// Simulate finishing activity permanently
		PresenterManager.activityLifecycleCallbacks.onActivityPaused(landscapeActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivityStopped(landscapeActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivitySaveInstanceState(landscapeActivity1, bundle1)
		PresenterManager.activityLifecycleCallbacks.onActivityDestroyed(landscapeActivity1)

		assertFalse(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(1, application.unregisterInvocations)
	}

	@Test
	fun saveActivityIdAndRestoreFromBundleWithTwoActivitiesOnBackStack() {
		val bundle1 = mock(Bundle::class.java)
		val bundle2 = mock(Bundle::class.java)
		val portraitActivity1 = mock(Activity::class.java)
		val landscapeActivity1 = mock(Activity::class.java)
		val portraitActivity2 = mock(Activity::class.java)
		val landscapeActivity2 = mock(Activity::class.java)
		val application = MockApplication()

		`when`(portraitActivity1.application).thenReturn(application)
		`when`(landscapeActivity1.application).thenReturn(application)
		`when`(portraitActivity2.application).thenReturn(application)
		`when`(landscapeActivity2.application).thenReturn(application)
		`when`(portraitActivity1.isChangingConfigurations).thenReturn(true)
		`when`(landscapeActivity1.isChangingConfigurations).thenReturn(false)
		`when`(portraitActivity2.isChangingConfigurations).thenReturn(true)
		`when`(landscapeActivity2.isChangingConfigurations).thenReturn(false)

		// This one also registers for lifecycle events
		val activity1ScopedCache1 = PresenterManager.getOrCreateActivityScopedCache(portraitActivity1)
		val activityId1 = PresenterManager.activityIdMap[portraitActivity1]
		`when`(bundle1.getString(PresenterManager.KEY_ACTIVITY_ID)).thenReturn(activityId1)

		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)

		// start second activity
		PresenterManager.activityLifecycleCallbacks.onActivityCreated(portraitActivity2, null)
		PresenterManager.activityLifecycleCallbacks.onActivityStarted(portraitActivity2)
		PresenterManager.activityLifecycleCallbacks.onActivityResumed(portraitActivity2)

		// This one also registers for lifecycle events
		val activity2ScopedCache1 = PresenterManager.getOrCreateActivityScopedCache(portraitActivity2)
		val activityId2 = PresenterManager.activityIdMap[portraitActivity2]
		`when`(bundle2.getString(PresenterManager.KEY_ACTIVITY_ID)).thenReturn(activityId2)

		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)

		// Simulate screen orientation change
		PresenterManager.activityLifecycleCallbacks.onActivityPaused(portraitActivity2)
		PresenterManager.activityLifecycleCallbacks.onActivityStopped(portraitActivity2)
		PresenterManager.activityLifecycleCallbacks.onActivitySaveInstanceState(portraitActivity2, bundle2)
		PresenterManager.activityLifecycleCallbacks.onActivityDestroyed(portraitActivity2)

		verify(bundle2).putString(PresenterManager.KEY_ACTIVITY_ID, activityId2)
		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)
		val activity2Id = bundle2.getString(PresenterManager.KEY_ACTIVITY_ID)
		assertNotNull(activity2Id)

		PresenterManager.activityLifecycleCallbacks.onActivityPaused(portraitActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivityStopped(portraitActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivitySaveInstanceState(portraitActivity1, bundle1)
		PresenterManager.activityLifecycleCallbacks.onActivityDestroyed(portraitActivity1)

		verify(bundle1).putString(PresenterManager.KEY_ACTIVITY_ID, activityId1)
		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)
		val activity1Id = bundle1.getString(PresenterManager.KEY_ACTIVITY_ID)
		assertNotNull(activity1Id)

		PresenterManager.activityLifecycleCallbacks.onActivityCreated(landscapeActivity2, bundle2)
		PresenterManager.activityLifecycleCallbacks.onActivityStarted(landscapeActivity2)
		PresenterManager.activityLifecycleCallbacks.onActivityResumed(landscapeActivity2)

		val activity2ScopedCache2 = PresenterManager.getOrCreateActivityScopedCache(landscapeActivity2)
		assertTrue(activity2ScopedCache1 == activity2ScopedCache2)

		PresenterManager.activityLifecycleCallbacks.onActivityCreated(landscapeActivity1, bundle1)
		PresenterManager.activityLifecycleCallbacks.onActivityStarted(landscapeActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivityResumed(landscapeActivity1)

		val activity1ScopedCache2 = PresenterManager.getOrCreateActivityScopedCache(landscapeActivity1)
		assertTrue(activity1ScopedCache1 == activity1ScopedCache2)

		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)

		// Simulate finishing activity2 permanently
		PresenterManager.activityLifecycleCallbacks.onActivityPaused(landscapeActivity2)
		PresenterManager.activityLifecycleCallbacks.onActivityStopped(landscapeActivity2)
		PresenterManager.activityLifecycleCallbacks.onActivitySaveInstanceState(landscapeActivity2, bundle2)
		PresenterManager.activityLifecycleCallbacks.onActivityDestroyed(landscapeActivity2)
		assertTrue(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(0, application.unregisterInvocations)

		// Simulate finishing activity1 permanently
		PresenterManager.activityLifecycleCallbacks.onActivityPaused(landscapeActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivityStopped(landscapeActivity1)
		PresenterManager.activityLifecycleCallbacks.onActivitySaveInstanceState(landscapeActivity1, bundle1)
		PresenterManager.activityLifecycleCallbacks.onActivityDestroyed(landscapeActivity1)
		assertFalse(application.lifecycleCallbacksList.contains(PresenterManager.activityLifecycleCallbacks))
		assertEquals(1, application.registerInvocations)
		assertEquals(1, application.unregisterInvocations)
	}

	@Test
	fun getActivityScopeReturnsNullIfNotExisting() {
		val activity = mock(Activity::class.java)
		assertNull(PresenterManager.getActivityScope(activity))
	}

	@Test
	fun getActivityScopeReturnsExistingOne() {
		val activity = mock(Activity::class.java)
		val application = mock(Application::class.java)

		`when`(activity.application).thenReturn(application)

		val scope1 = PresenterManager.getOrCreateActivityScopedCache(activity)
		assertNotNull(scope1)
		assertEquals(scope1, PresenterManager.getActivityScope(activity))
	}

	@Test
	fun getPresenterReturnsNull() {
		val activity = mock(Activity::class.java)
		val application = mock(Application::class.java)
		`when`(activity.application).thenReturn(application)

		assertNull(PresenterManager.getPresenter(activity, "viewId123"))
	}

	@Test
	fun getViewStateReturnsNull() {
		val activity = mock(Activity::class.java)
		val application = mock(Application::class.java)
		`when`(activity.application).thenReturn(application)

		assertNull(PresenterManager.getViewState(activity, "viewId123"))
	}

	@Test
	fun putGetRemovePresenter() {
		val activity = mock(Activity::class.java)
		val application = mock(Application::class.java)
		`when`(activity.application).thenReturn(application)

		val presenter = object : MviPresenter<MviView, Any> {
			override fun attachView(view: MviView) = Unit

			override fun detachView() = Unit

			override fun destroy() = Unit
		}

		val viewId = "123"
		assertNull(PresenterManager.getPresenter(activity, viewId))

		PresenterManager.putPresenter(activity, viewId, presenter)
		assertTrue(presenter === PresenterManager.getPresenter<MviPresenter<MviView, Any>>(activity, viewId))

		PresenterManager.remove(activity, viewId)
		assertNull(PresenterManager.getPresenter(activity, viewId))
	}


	@Test
	fun putGetRemoveViewState() {
		val activity = mock(Activity::class.java)
		val application = mock(Application::class.java)
		`when`(activity.application).thenReturn(application)

		val viewState = Any()

		val viewId = "123"
		assertNull(PresenterManager.getViewState(activity, viewId))

		PresenterManager.putViewState(activity, viewId, viewState)
		assertTrue(viewState === PresenterManager.getViewState<Any>(activity, viewId))

		PresenterManager.remove(activity, viewId)
		assertNull(PresenterManager.getPresenter(activity, viewId))
	}
}
