@file:Suppress("NOTHING_TO_INLINE")

package me.reezy.cosmo.flowbus

import androidx.lifecycle.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap


@Suppress("UNCHECKED_CAST")
class EventBus : ViewModel() {

    companion object {
        val global = EventBus()


        inline fun emitString(eventName: String) {
            global.emit(String::class.java, eventName, eventName)
        }

        inline fun <reified T> emit(eventName: String, value: T) {
            global.emit(T::class.java, eventName, value)
        }

        inline fun <reified T> emit(value: T) {
            global.emit(T::class.java, "", value)
        }

        inline fun <reified T> observe(owner: LifecycleOwner, eventName: String = "", minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
            global.observe(T::class.java, owner, eventName, minState, observer)
        }

        inline fun <reified T> observeLatest(owner: LifecycleOwner, eventName: String = "", minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
            global.observeLatest(T::class.java, owner, eventName, minState, observer)
        }

        inline fun <reified T> observeForever(eventName: String = "", noinline observer: (T) -> Unit) {
            global.observeForever(T::class.java, eventName, observer)
        }
    }

    private val bus by lazy { ConcurrentHashMap<String, MutableSharedFlow<*>>() }

    fun emitString(eventName: String) {
        emit(String::class.java, eventName, eventName)
    }

    inline fun <reified T> emit(eventName: String, value: T) {
        emit(T::class.java, eventName, value)
    }

    inline fun <reified T> emit(value: T) {
        emit(T::class.java, "", value)
    }

    inline fun <reified T> observe(owner: LifecycleOwner, eventName: String = "", minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
        observe(T::class.java, owner, eventName, minState, observer)
    }

    inline fun <reified T> observeLatest(owner: LifecycleOwner, eventName: String = "", minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
        observeLatest(T::class.java, owner, eventName, minState, observer)
    }

    inline fun <reified T> observeForever(eventName: String = "", noinline observer: (T) -> Unit) {
        observeForever(T::class.java, eventName, observer)
    }


    fun <T> emit(eventClazz: Class<T>, eventName: String, value: T) {
        val key = "$eventClazz:$eventName"
        viewModelScope.launch {
            (bus[key] as? MutableSharedFlow<T>)?.emit(value)
        }
    }


    fun <T> observe(eventClazz: Class<T>, owner: LifecycleOwner, eventName: String, minState: Lifecycle.State = Lifecycle.State.STARTED, action: (T) -> Unit) {
        get<T>("$eventClazz:$eventName", owner).observe(owner, minState, action)
    }

    fun <T> observeLatest(eventClazz: Class<T>, owner: LifecycleOwner, eventName: String, minState: Lifecycle.State = Lifecycle.State.STARTED, action: (T) -> Unit) {
        get<T>("$eventClazz:$eventName", owner).observeLatest(owner, minState, action)
    }

    fun <T> observeForever(eventClazz: Class<T>, eventName: String, observer: (T) -> Unit) {
        viewModelScope.launch {
            get<T>("$eventClazz:$eventName").collect {
                try {
                    observer(it)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }
    }


    private fun <T> get(key: String, owner: LifecycleOwner? = null): Flow<T> {
        val flow = bus.getOrPut(key) { MutableSharedFlow<T>(0, 1, BufferOverflow.DROP_OLDEST) } as MutableSharedFlow<T>

        owner?.lifecycle?.addObserver(LifecycleEventObserver { _, lifecycleEvent ->
            if (lifecycleEvent == Lifecycle.Event.ON_DESTROY) {
                if (flow.subscriptionCount.value <= 0) {
                    bus.remove(key)
                }
            }
        })

        return flow
    }

}
