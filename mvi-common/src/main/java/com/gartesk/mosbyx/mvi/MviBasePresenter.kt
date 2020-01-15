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
package com.gartesk.mosbyx.mvi

import androidx.annotation.CallSuper
import androidx.annotation.MainThread

import com.gartesk.mosbyx.mvp.MvpView

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.reactivex.subjects.UnicastSubject

/**
 * This type of presenter is responsible for interaction with the viewState in a Model-View-Intent way.
 * It is the bridge that is responsible for setting up the reactive flow between "view" and "model".
 *
 * The methods [bindIntents] and [unbindIntents] are kind of representing the lifecycle of this Presenter.
 *
 *  * [bindIntents] is called the first time the view is attached
 *  * [unbindIntents] is called once the view is detached permanently because the view
 * has been destroyed and hence this presenter is not needed anymore and will also be destroyed
 * afterwards too.
 *
 * This means that a presenter can survive orientation changes. During orientation changes (or when
 * the view is put on the back stack because the user navigated to another view) the view
 * will be detached temporarily and reattached to the presenter afterwards. To avoid memory leaks
 * this Presenter class offers two methods:
 *
 *  * [intent]: Use this to bind an Observable intent from the view
 *  * [subscribeViewState]: Use this to bind the ViewState. A viewState is an object
 *  (typically a POJO) that holds all the data the view needs to display
 *
 * By using [intent] and [subscribeViewState] a relay will be established between the view and
 * this presenter that allows the view to be temporarily detached, without unsubscribing
 * the underlying reactive business logic workflow and without causing memory leaks
 * (caused by recreation of the view).
 *
 * Please note that the methods [attachView] and [detachView] should not be overridden unless
 * you have a really good reason to do so. Usually [bindIntents] and [unbindIntents] should be enough.
 *
 * In very rare cases you could also use [.getViewStateObservable] to offer an observable
 * to other components you can make this method public.
 *
 * **Please note that you should not reuse an MviBasePresenter once the View that originally has
 * instantiated this Presenter has been destroyed permanently**. App-wide singletons for
 * Presenters is not a good idea in Model-View-Intent. Reusing singleton-scoped Presenters for
 * different view instances may cause emitting the previous state of the previous attached view
 * (which already has been destroyed permanently).
 *
 * @param [V] The type of the view this presenter responds to
 * @param [VS] The type of the viewState state
 */
abstract class MviBasePresenter<V : MvpView, VS>
/**
 * Creates a new Presenter with the initial view state
 *
 * @param initialViewState initial view state (must be not null)
 */
constructor(initialViewState: VS? = null) : MviPresenter<V, VS> {

	/**
	 * This relay is the bridge to the viewState (UI). Whenever the viewState gets re-attached, the
	 * latest state will be re-emitted.
	 */
	private val viewStateBehaviorSubject: BehaviorSubject<VS> =
		if (initialViewState == null) {
			BehaviorSubject.create()
		} else {
			BehaviorSubject.createDefault(initialViewState)
		}

	/**
	 * We only allow to call [subscribeViewState] method once
	 */
	private var subscribeViewStateMethodCalled = false

	/**
	 * List of internal relays, bridging the gap between intents coming from the viewState (will be
	 * unsubscribed temporarily when viewState is detached i.e. during config changes)
	 */
	private val intentRelaysBinders = mutableListOf<IntentRelayBinderPair<*>>()

	/**
	 * Composite Disposables holding subscriptions to all intents observable offered by the viewState.
	 */
	private val intentDisposables = CompositeDisposable()

	/**
	 * Disposable to unsubscribe from the viewState when the viewState is detached
	 * (i.e. during screen orientation changes)
	 */
	private var viewRelayConsumerDisposable: Disposable? = null

	/**
	 * Disposable between the viewState observable returned from [intent] and [viewStateBehaviorSubject]
	 */
	private var viewStateDisposable: Disposable? = null

	/**
	 * Used to determine whether or not a View has been attached for the first time.
	 * This is used to determine whether or not the intents should be bound via [bindIntents] or rebound internally.
	 */
	private var viewAttachedFirstTime = true

	/**
	 * This binder is used to subscribe the view's render method to render the ViewState in the view.
	 */
	private var viewStateConsumer: ViewStateConsumer<V, VS>? = null

	init {
		reset()
	}

	/**
	 * Gets the view state observable.
	 *
	 * Most likely you will use this property for unit testing your presenter.
	 *
	 * In some very rare case it could be useful to provide other components, such as other presenters,
	 * access to the state. This observable contains the same value as the one from [subscribeViewState]
	 * which is also used to render the view. In other words, this observable also represents
	 * the state of the View, so you could subscribe via this observable to the view's state.
	 *
	 * @return [Observable]
	 */
	open val viewStateObservable: Observable<VS>
		get() = viewStateBehaviorSubject
	/**
	 * The binder is responsible for binding a single view intent.
	 * Typically, you use that in [bindIntents] in combination with the [intent] function
	 *
	 * @param [V] The View type
	 * @param [I] The type of the Intent
	 */
	protected interface ViewIntentBinder<V : MvpView, I> {
		fun bind(view: V): Observable<I>
	}

	/**
	 * This "binder" is responsible for binding the view state to the currently attached view.
	 * This typically "renders" the view.
	 *
	 * Typically, this is used in [bindIntents] with [subscribeViewState]
	 *
	 * @param [V] The view type
	 * @param [VS] The ViewState type
	 */
	protected interface ViewStateConsumer<V : MvpView, VS> {
		fun accept(view: V, viewState: VS)
	}

	/**
	 * A simple class that holds a pair of the Intent relay and the binder to bind the
	 * actual Intent Observable.
	 *
	 * @param [I] The Intent type
	 */
	private inner class IntentRelayBinderPair<I>(
		internal val intentRelaySubject: Subject<I>,
		internal val intentBinder: ViewIntentBinder<V, I>
	)

	@CallSuper
	override fun attachView(view: V) {
		if (viewAttachedFirstTime) {
			bindIntents()
		}

		// Build the chain from bottom to top:
		// 1. Subscribe to ViewState
		// 2. Subscribe intents
		viewStateConsumer?.let { consumer ->
			subscribeViewStateConsumerActually(view, consumer)
		}

		intentRelaysBinders.forEach {
			bindIntentActually<Any>(view, it)
		}

		viewAttachedFirstTime = false
	}

	@CallSuper
	override fun detachView() {
		// Cancel subscription from View to viewState Relay
		viewRelayConsumerDisposable?.dispose()
		viewRelayConsumerDisposable = null

		// Cancel subscriptions from view intents to intent Relays
		intentDisposables.clear()
	}

	@CallSuper
	override fun destroy() {
		// Cancel the overall observable stream
		viewStateDisposable?.dispose()

		unbindIntents()
		reset()
		// TODO should we re-emit the initial state? What if no initial state has been set?
		// TODO should we rather throw an exception if presenter is reused after view has been detached permanently
	}

	/**
	 * This is called when the View has been detached permanently (view is destroyed permanently)
	 * to reset the internal state of this Presenter to be ready for being reused (even though
	 * reusing presenters after their view has been destroyed is BAD)
	 */
	private fun reset() {
		viewAttachedFirstTime = true
		intentRelaysBinders.clear()
		subscribeViewStateMethodCalled = false
	}

	/**
	 * This method subscribes the Observable emitting `ViewState` over time to the passed
	 * consumer.
	 * **Only invoke this method once!**
	 *
	 * Internally, Mosby will hold some relays to ensure that no items emitted from the ViewState
	 * Observable will be lost while viewState is not attached nor that the subscriptions to
	 * viewState intents will cause memory leaks while viewState detached.
	 *
	 * Typically, this method is used in [bindIntents]
	 *
	 * @param viewStateObservable The Observable emitting new ViewState. Typically,
	 * an intent [intent] causes the underlying business logic to do a change and eventually
	 * create a new ViewState.
	 * @param consumer [ViewStateConsumer] The consumer that will update ("render") the view.
	 */
	@MainThread
	protected fun subscribeViewState(
		viewStateObservable: Observable<VS>,
		consumer: ViewStateConsumer<V, VS>
	) {
		check(!subscribeViewStateMethodCalled) { "subscribeViewState() method is only allowed to be called once" }
		subscribeViewStateMethodCalled = true

		this.viewStateConsumer = consumer

		viewStateDisposable = viewStateObservable.subscribeWith(
			DisposableViewStateObserver(viewStateBehaviorSubject)
		)
	}

	/**
	 * This method is a kotlin version for [subscribeViewState] with lambda for argument
	 *
	 * @param viewStateObservable The Observable emitting new ViewState. Typically,
	 * an intent [ ][.intent] causes the underlying business logic to do a change and eventually
	 * create a new ViewState.
	 * @param consumer lambda version of [ViewStateConsumer] The consumer that will update ("render") the view.
	 */
	@MainThread
	protected fun subscribeViewState(
		viewStateObservable: Observable<VS>,
		consumer: (V, VS) -> Unit
	) {
		val consumerObject = object: ViewStateConsumer<V, VS> {
			override fun accept(view: V, viewState: VS) {
				consumer(view, viewState)
			}
		}
		return subscribeViewState(viewStateObservable, consumerObject)
	}

	/**
	 * Actually subscribes the view as consumer to the internally view relay.
	 *
	 * @param view The mvp view
	 * @param consumer [ViewStateConsumer] The consumer that will update ("render") the view.
	 */
	@MainThread
	private fun subscribeViewStateConsumerActually(view: V, consumer: ViewStateConsumer<V, VS>) {
		viewRelayConsumerDisposable = viewStateBehaviorSubject
			.subscribe { vs -> consumer.accept(view, vs) }
	}

	/**
	 * This method is called once the view is attached to this presenter for the very first time.
	 * For instance, it will not be called again during screen orientation changes when the view will be
	 * detached temporarily.
	 *
	 * The counter part of this method is [unbindIntents].
	 * These methods [bindIntents] and [unbindIntents] are kind of representing the
	 * lifecycle of this Presenter.
	 * [bindIntents] is called the first time the view is attached and [unbindIntents] is called
	 * once the view is detached permanently because it has been destroyed and hence this presenter
	 * is not needed anymore and will also be destroyed afterwards
	 */
	@MainThread
	protected abstract fun bindIntents()

	/**
	 * This method will be called once the view has been detached permanently and hence the presenter
	 * will be "destroyed" too. This is the correct time for doing some cleanup like unsubscribe from
	 * RxSubscriptions, etc.
	 *
	 * The counter part of this method is [bindIntents].
	 * These methods [bindIntents] and [unbindIntents] are kind of representing the
	 * lifecycle of this Presenter.
	 * [bindIntents] is called the first time the view is attached and [unbindIntents] is called
	 * once the view is detached permanently because it has been destroyed and hence this presenter
	 * is not needed anymore and will also be destroyed afterwards
	 */
	protected open fun unbindIntents() = Unit

	/**
	 * This method creates a decorator around the original view's "intent". This method ensures that
	 * no memory leak by using a [ViewIntentBinder] is caused by the subscription to the original
	 * view's intent when the view gets detached.
	 *
	 * Typically, this method is used in [bindIntents]
	 *
	 * @param binder The [ViewIntentBinder] from where the the real view's intent will be bound
	 * @param [I] The type of the intent
	 * @return The decorated intent Observable emitting the intent
	 */
	@MainThread
	protected fun <I> intent(binder: ViewIntentBinder<V, I>): Observable<I> {
		val intentRelay = UnicastSubject.create<I>()
		intentRelaysBinders.add(IntentRelayBinderPair(intentRelay, binder))
		return intentRelay
	}

	/**
	 * This method is a kotlin version for [intent] with lambda for argument
	 *
	 * @param binder The lambda version of [ViewIntentBinder] from where the the real view's intent
	 * will be bound
	 * @param [I] The type of the intent
	 * @return The decorated intent Observable emitting the intent
	 */
	@MainThread
	protected fun <I> intent(binder: (V) -> Observable<I>): Observable<I> {
		val binderObject = object : ViewIntentBinder<V, I> {
			override fun bind(view: V): Observable<I> = binder(view)
		}
		return intent(binderObject)
	}

	@MainThread
	private fun <I> bindIntentActually(
		view: V,
		relayBinderPair: IntentRelayBinderPair<*>
	): Observable<I> {
		val intentRelay = relayBinderPair.intentRelaySubject as Subject<I>
		val intentBinder = relayBinderPair.intentBinder as ViewIntentBinder<V, I>
		val intent = intentBinder.bind(view)
		intentDisposables.add(intent.subscribeWith(DisposableIntentObserver(intentRelay)))
		return intentRelay
	}
}
