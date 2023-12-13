package me.reezy.cosmo.flowbus

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun <T> EventFlow() = MutableSharedFlow<T>(0, 1, BufferOverflow.DROP_OLDEST)


/**
 * 订阅事件(EventFlow)或状态(StateFlow)
 *
 * - 对于 [EventFlow]，进入 [minState] 开始订阅，脱离 [minState] 取消订阅
 * - 对于 [StateFlow]，满足 [minState] 时立刻消费，不足 [minState] 时缓存最近的事件待再次进入时消费
 *
 */
fun <T> Flow<T>.observe(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit) {
    if (this is StateFlow) {
        observeLatest(owner, minState, action)
    } else {
        observeRepeat(owner, minState, action)
    }
}

/**
 * 订阅事件，满足 [minState] 时立刻消费，不足 [minState] 时缓存最近的事件待再次进入时消费
 *
 * 示例：在A页观察流，然后进入B页发送了事件，回到A页消费事件
 */
fun <T> Flow<T>.observeLatest(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit) {
    var data: T? = null
    owner.lifecycleScope.launch {
        owner.lifecycle.repeatOnLifecycle(minState) {
            if (data != null) {
                action(data!!)
                data = null
            }
        }
    }
    owner.lifecycleScope.launch {
        collect {
            if (owner.lifecycle.currentState >= minState) {
                data = null
                action(it)
            } else {
                data = it
            }
        }
    }
}


/**
 * 进入 [minState] 开始订阅，脱离 [minState] 取消订阅
 * */
fun <T> Flow<T>.observeRepeat(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit) {
    owner.lifecycleScope.launch {
        owner.lifecycle.repeatOnLifecycle(minState) {
            collect(action)
        }
    }
}


