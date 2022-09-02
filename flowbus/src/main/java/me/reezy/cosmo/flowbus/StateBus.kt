@file:Suppress("NOTHING_TO_INLINE")

package me.reezy.cosmo.flowbus

import androidx.annotation.RestrictTo
import androidx.lifecycle.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap


@Suppress("UNCHECKED_CAST")
class StateBus : ViewModel() {

    companion object {
        val global = StateBus()

        fun <T> get(key: BusKey<T>): T? {
            return global.get(key)
        }

        fun <T> set(key: BusKey<T>, value: T) {
            global.set(key, value)
        }

        fun <T> update(key: BusKey<T>, updater: (T?) -> T) {
            global.update(key, updater)
        }

        inline fun <reified T> observe(owner: LifecycleOwner, key: BusKey<T>, minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
            global.observe(key, owner, minState, observer)
        }

        inline fun <reified T> observeForever(key: BusKey<T>, noinline observer: (T) -> Unit) {
            global.observe(key, null, Lifecycle.State.STARTED, observer)
        }


        inline fun <reified T> get(name: String): T? {
            return global.get(BusKey(name, T::class.java))
        }

        inline fun <reified T> set(name: String, value: T) {
            global.set(BusKey(name, T::class.java), value)
        }

        inline fun <reified T> update(name: String, noinline updater: (T?) -> T) {
            val key = busKey<T>(name)
            global.update(key, updater)
        }

        inline fun <reified T> observe(owner: LifecycleOwner, name: String, minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
            global.observe(BusKey(name, T::class.java), owner, minState, observer)
        }

        inline fun <reified T> observeForever(name: String = "", noinline observer: (T) -> Unit) {
            global.observe(BusKey(name, T::class.java), null, Lifecycle.State.STARTED, observer)
        }
    }

    private val bus by lazy { ConcurrentHashMap<BusKey<*>, MutableStateFlow<*>>() }


    inline fun <reified T> get(name: String): T? {
        return get(BusKey(name, T::class.java))
    }

    inline fun <reified T> set(name: String, value: T) {
        set(BusKey(name, T::class.java), value)
    }

    inline fun <reified T> update(name: String, noinline updater: (T?) -> T) {
        update(BusKey(name, T::class.java), updater)
    }

    inline fun <reified T> observe(owner: LifecycleOwner, name: String = "", minState: Lifecycle.State = Lifecycle.State.STARTED, noinline observer: (T) -> Unit) {
        observe(BusKey(name, T::class.java), owner, minState, observer)
    }

    inline fun <reified T> observeForever(name: String = "", noinline observer: (T) -> Unit) {
        observe(BusKey(name, T::class.java), null, Lifecycle.State.STARTED, observer)
    }



    fun <T> update(key: BusKey<T>, updater: (T?) -> T) {
        set(key, updater(get(key)))
    }

    fun <T> observeForever(key: BusKey<T>, observer: (T) -> Unit) {
        observe(key, null, Lifecycle.State.STARTED, observer)
    }

    fun <T> get(key: BusKey<T>): T? {
        return (bus[key] as? MutableStateFlow<T>)?.value
    }

    fun <T> set(key: BusKey<T>, value: T) {
        viewModelScope.launch {
            val flow = ensure(key)
            if (flow.value != value) {
                flow.emit(value)
            }
        }
    }

    fun <T> observe(key: BusKey<T>, owner: LifecycleOwner? = null, minState: Lifecycle.State = Lifecycle.State.STARTED, action: (T) -> Unit) {

        val collector = FlowCollector<T?> {
            if (it != null) {
                try {
                    action(it as T)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }

        val flow = ensure(key)

        if (owner != null) {
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

    private fun <T> ensure(key: BusKey<T>) = bus.getOrPut(key) { MutableStateFlow<T?>(null) } as MutableStateFlow<T?>
}
