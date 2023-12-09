package me.reezy.cosmo.flowbus

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun <T> EventFlow() = MutableSharedFlow<T>(0, 1, BufferOverflow.DROP_OLDEST)

/**
 * 观察流，进入 [minState] 时开始，脱离 [minState] 时停止
 *
 * - 对于 [StateFlow]，每次进入 [minState] 都会观察一次当前值
 * - 对于 [EventFlow]，只能收到进入 [minState] 后发射的值
 * */
fun <T> Flow<T>.observe(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit) {
    owner.lifecycleScope.launch {
        owner.lifecycle.repeatOnLifecycle(minState) {
            collect(action)
        }
    }
}

/**
 * 观察流，脱离 [minState] 后会保存最近产生的值，再次进入 [minState] 时消费
 *
 * - 对于 [StateFlow]，没啥用，效果与 [observe] 没区别
 * - 对于 [EventFlow]，每次进入 [minState] 时能收到并消费最近产生的值(如果有)
 *
 * [EventFlow] 场景：在A页观察流，然后进入B页发送了事件，但想回到A页时再处理，可用此方法
 * */
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


