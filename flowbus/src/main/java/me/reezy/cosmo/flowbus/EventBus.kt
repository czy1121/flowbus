@file:Suppress("NOTHING_TO_INLINE")

package me.reezy.cosmo.flowbus

import androidx.lifecycle.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap


@Suppress("UNCHECKED_CAST")
class EventBus : ViewModel() {

    companion object {
        val global = EventBus()

        inline fun <T> emit(key: BusKey<T>, value: T) {
            global.emit(key, value)
        }

        inline fun <T> observe(owner: LifecycleOwner, key: BusKey<T>, minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
            global.observe(key, owner, minState, observer)
        }

        inline fun <T> observeForever(key: BusKey<T>, noinline observer: (T) -> Unit) {
            global.observe(key, null, Lifecycle.State.STARTED, observer)
        }

        inline fun emitString(name: String) {
            global.emit(BusKey(name, String::class.java), name)
        }

        inline fun <reified T> emit(name: String, value: T) {
            global.emit(BusKey(name, T::class.java), value)
        }

        inline fun <reified T> emit(value: T) {
            global.emit(BusKey("", T::class.java), value)
        }

        inline fun <reified T> observe(owner: LifecycleOwner, name: String = "", minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
            global.observe(BusKey(name, T::class.java), owner, minState, observer)
        }

        inline fun <reified T> observeForever(name: String = "", noinline observer: (T) -> Unit) {
            global.observe(BusKey(name, T::class.java), null, Lifecycle.State.STARTED, observer)
        }
    }

    private val bus by lazy { ConcurrentHashMap<BusKey<*>, MutableSharedFlow<*>>() }

    fun emitString(name: String) {
        emit(BusKey(name, String::class.java), name)
    }

    inline fun <reified T> emit(name: String, value: T) {
        emit(BusKey(name, T::class.java), value)
    }

    inline fun <reified T> emit(value: T) {
        emit(BusKey("", T::class.java), value)
    }

    inline fun <reified T> observe(owner: LifecycleOwner, name: String = "", minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
        observe(BusKey(name, T::class.java), owner, minState, observer)
    }

    inline fun <reified T> observeForever(name: String = "", noinline observer: (T) -> Unit) {
        observe(BusKey(name, T::class.java), null, Lifecycle.State.STARTED, observer)
    }


    fun <T> observeForever(key: BusKey<T>, observer: (T) -> Unit) {
        observe(key, null, Lifecycle.State.STARTED, observer)
    }

    fun <T> emit(key: BusKey<T>, value: T) {
        viewModelScope.launch {
            (bus[key] as? MutableSharedFlow<T>)?.emit(value)
        }
    }

    fun <T> observe(key: BusKey<T>, owner: LifecycleOwner? = null, minState: Lifecycle.State = Lifecycle.State.STARTED, action: (T) -> Unit) {

        val collector = FlowCollector<T> {
            try {
                action(it)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }

        val flow = bus.getOrPut(key) { MutableSharedFlow<T>(0, 1, BufferOverflow.DROP_OLDEST) } as MutableSharedFlow<T>

        if (owner != null) {
            owner.lifecycle.addObserver(LifecycleEventObserver { _, lifecycleEvent ->
                if (lifecycleEvent == Lifecycle.Event.ON_DESTROY) {
                    if (flow.subscriptionCount.value <= 0) {
                        bus.remove(key)
                    }
                }
            })

            owner.lifecycleScope.launch {
                owner.lifecycle.whenStateAtLeast(minState) {
                    flow.collect(collector)
                }
            }
        } else {
            viewModelScope.launch {
                flow.collect(collector)
            }
        }
    }
}
