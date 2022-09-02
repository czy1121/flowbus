# flowbus

事件总线(基于SharedFlow) 和 状态总线(基于StateFlow)

事件总线

- 支持事件操作 emit/observe
- 支持生命周期感知
- 支持自动清除空闲事件
- 不支持 Sticky 事件

状态总线

- 支持状态操作 get/set/observe
- 支持生命周期感知

## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:flowbus:0.7.0"
}
```

## EventBus 使用

```kotlin
data class FooEvent(val value: String)

EventBus.observe<FooEvent>(this) {
    log("main => FooEvent(value = ${it.value})")
}

binding.foo.setOnClickListener {
    EventBus.emit(FooEvent("clicked in main"))
}
```

## StateBus 使用

```kotlin

StateBus.set("what", 10)

StateBus.observe<Int>(this, "what") {
    binding.state.text = "state = $it"
}

StateBus.update<Int>("what") {
    (it ?: 0) + 1
}
```

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).