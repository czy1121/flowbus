package com.demo.app

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import me.reezy.cosmo.flowbus.SimpleEventFlow

object Global {

    val eventFloat =  SimpleEventFlow<Float>()

    val eventString = SimpleEventFlow<String>()

    val eventFoo = SimpleEventFlow<FooEvent>()

    val stateCount = MutableStateFlow(0)
}