package com.demo.app

import kotlinx.coroutines.flow.MutableStateFlow
import me.reezy.cosmo.flowbus.EventFlow

object Global {

    val eventFloat =  EventFlow<Float>()

    val eventString = EventFlow<String>()

    val eventFoo = EventFlow<FooEvent>()

    val stateCount = MutableStateFlow(0)
}