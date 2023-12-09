# flowbus

事件总线(基于SharedFlow)
 
- 支持生命周期感知
- 支持自动清除空闲事件 


## 观察

```kotlin

/**
 * 观察流，进入 [minState] 时开始，脱离 [minState] 时停止
 *
 * - 对于 [StateFlow]，每次进入 [minState] 都会观察一次当前值
 * - 对于 [EventFlow]，只能收到进入 [minState] 后发射的值
 * */
fun <T> Flow<T>.observe(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit)

/**
 * 观察流，脱离 [minState] 后会保存最近产生的值，再次进入 [minState] 时消费
 *
 * - 对于 [StateFlow]，没啥用，效果与 [observe] 没区别
 * - 对于 [EventFlow]，每次进入 [minState] 时能收到并消费最近产生的值(如果有)
 * 
 * [EventFlow] 场景：在A页订阅事件，然后进入B页发送了事件，但想回到A页时再处理，可用此方法
 * */
fun <T> Flow<T>.observeLatest(owner: LifecycleOwner, minState: Lifecycle.State = Lifecycle.State.STARTED, action: suspend (T) -> Unit)
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
    implementation "me.reezy.cosmo:flowbus:0.8.0"
}
```

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).