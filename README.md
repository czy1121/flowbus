# flowbus

事件总线(基于SharedFlow)
 
- 支持生命周期感知
- 支持自动清除空闲事件 

事件(SimpleEventFlow)和状态(MutableStateFlow)

- 订阅事件(`Flow<T>.observeEvent`)
  - 通过 `Lifecycle.repeatOnLifecycle` 实现
  - 订阅后进入 `minState` 状态开始接收处理事件, 脱离 `minState` 状态停止接收处理事件  
  - 通常配合 `SimpleEventFlow` 使用
- 订阅状态(`Flow<T>.observeState`)
  - 通过 `Lifecycle.whenStateAtLeast` 实现
  - 订阅后开始接收事件，但不会立刻处理，进入 `minState` 状态后处理最新的一个事件
  - 通常配合 `MutableStateFlow` 使用 


有时，在A页订阅事件`SimpleEventFlow`，然后进入B页发送了事件，但想回到A页时再处理，可用 `observeState`   

## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:flowbus:0.8.0"
}
```

## EventBus 使用

```kotlin
data class FooEvent(val value: String)
data class BarEvent(val value: String)

object Global {
    val eventFoo = SimpleEventFlow<FooEvent>()

    val stateCount = MutableStateFlow(0)
}

Global.stateCount.observeState(this) {
    binding.state.text = "count = $it"
}
Global.eventFoo.observeState(this) {
    log("Global => $it")
} 
EventBus.observeEvent<BarEvent>(this) {
    log("EventBus => $it")
}

binding.fooEvent.setOnClickListener {
    Global.eventFoo.tryEmit(FooEvent("clicked in main"))
}
binding.barEvent.setOnClickListener {
    EventBus.emit(BarEvent("clicked in main"))
}
```


## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).