package com.demo.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.demo.app.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import me.reezy.cosmo.flowbus.EventBus
import me.reezy.cosmo.flowbus.observeEvent
import me.reezy.cosmo.flowbus.observeState
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Global.stateCount.observeState(this) {
            binding.state.text = "count = $it"
            log("count = $it")
        }

        Global.eventFoo.observeEvent(this) {
            log("Global foo1 => $it")
        }
        Global.eventFoo.observeState(this) {
            log("Global foo2 sticky => $it")
        }
        Global.eventFloat.observeEvent(this) {
            log("Global float event => $it")
        }
        Global.eventString.observeEvent(this) {
            log("Global => $it")
        }


        EventBus.observeState<BarEvent>(this) {
            log("EventBus => $it")
        }
        EventBus.observeState<Float>(this) {
            log("EventBus => $it")
        }
        EventBus.observeState<String>(this) {
            log("EventBus => $it")
        }

        binding.state.setOnClickListener {
            Global.stateCount.value += 1
        }
        binding.fooEvent.setOnClickListener {
            Global.eventFoo.tryEmit(FooEvent("clicked in main"))
        }
        binding.fooFloat.setOnClickListener {
            Global.eventFloat.tryEmit(Random.nextFloat())
        }
        binding.fooString.setOnClickListener {
            Global.eventString.tryEmit("clicked in main")
        }

        binding.barEvent.setOnClickListener {
            EventBus.emit(BarEvent("clicked in main"))
        }
        binding.barFloat.setOnClickListener {
            EventBus.emit(Random.nextFloat())
        }
        binding.barString.setOnClickListener {
            EventBus.emit("clicked in main")
        }

        binding.second.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

    }

    private fun log(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("OoO", "$this => $message")
        binding.logs.text = "${binding.logs.text}\n$message"
    }
}