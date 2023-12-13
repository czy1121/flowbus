# flowbus

事件总线(基于SharedFlow)
 
- 支持生命周期感知
- 支持自动清除空闲事件


## 观察

```kotlin

/**
 * 订阅事件(EventFlow)或状态(StateFlow)
 *
 * - 对于 [EventFlow]，进入 [minState] 开始订阅，脱离 [minState] 取消订阅
 * - 对于 [StateFlow]，满足 [minState] 时立刻消费，不足 [minState] 时缓存最近的事件待再次进入时消费
 *
 */
fun <T> Flow<T>.observe(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit)


/**
 * 订阅事件，满足 [minState] 时立刻消费，不足 [minState] 时缓存最近的事件待再次进入时消费
 *
 * 示例：在A页观察流，然后进入B页发送了事件，回到A页消费事件
 */
fun <T> Flow<T>.observeLatest(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit)


/**
 * 进入 [minState] 开始订阅，脱离 [minState] 取消订阅
 * */
fun <T> Flow<T>.observeRepeat(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit)
```

## EventBus 使用

```kotlin
data class FooEvent(val value: String)
data class BarEvent(val value: String)

object Global {
    val eventFoo = EventFlow<FooEvent>()

    val stateCount = MutableStateFlow(0)
}

Global.stateCount.observe(this) {
    binding.state.text = "count = $it"
}
Global.eventFoo.observeLatest(this) {
    log("Global => $it")
} 
EventBus.observe<BarEvent>(this) {
    log("EventBus => $it")
}

binding.fooEvent.setOnClickListener {
    Global.eventFoo.tryEmit(FooEvent("clicked in main"))
}
binding.barEvent.setOnClickListener {
    EventBus.emit(BarEvent("clicked in main"))
}
```


## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:flowbus:0.9.0"
}
```

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).