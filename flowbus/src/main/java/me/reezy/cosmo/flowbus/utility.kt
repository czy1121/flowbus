package me.reezy.cosmo.flowbus

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


inline fun <reified T> busKey(name: String) = BusKey(name, T::class.java)

fun <T> Flow<T>.observe(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: (T) -> Unit) {
    owner.lifecycleScope.launch {
        owner.lifecycle.whenStateAtLeast(minState) {
            collect(action)
        }
    }
}
