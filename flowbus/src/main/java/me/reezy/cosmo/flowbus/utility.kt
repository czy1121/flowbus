package me.reezy.cosmo.flowbus

import androidx.lifecycle.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun <T> SimpleEventFlow() = MutableSharedFlow<T>(0, 1, BufferOverflow.DROP_OLDEST)


//fun <T> StateFlow<T>.observeState(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit) {
//    owner.lifecycleScope.launch {
//        owner.lifecycle.whenStateAtLeast(minState) {
//            collect(action)
//        }
//    }
//}


fun <T> Flow<T>.observeState(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit) {
    owner.lifecycleScope.launch {
        owner.lifecycle.whenStateAtLeast(minState) {
            try {
                collect(action)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
}

fun <T> Flow<T>.observeEvent(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit) {
    owner.lifecycleScope.launch {
        owner.lifecycle.repeatOnLifecycle(minState) {
            try {
                collect(action)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }
}

