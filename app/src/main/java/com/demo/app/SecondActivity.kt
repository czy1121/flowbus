package com.demo.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.demo.app.databinding.ActivityMainBinding
import me.reezy.cosmo.flowbus.EventBus
import me.reezy.cosmo.flowbus.observe
import me.reezy.cosmo.flowbus.observeLatest
import kotlin.random.Random

class SecondActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Global.stateCount.observeLatest(this) {
            binding.state.text = "count = $it"
            log("count = $it")
        }

        Global.eventFoo.observe(this) {
            log("Global => $it")
        }
        Global.eventFloat.observe(this) {
            log("Global float event => $it")
        }
        Global.eventString.observe(this) {
            log("Global => $it")
        }


        EventBus.observe<BarEvent>(this) {
            log("EventBus => $it")
        }
        EventBus.observe<Float>(this) {
            log("EventBus => $it")
        }
        EventBus.observe<String>(this) {
            log("EventBus => $it")
        }



        binding.state.setOnClickListener {
            Global.stateCount.value += 1
        }
        binding.fooEvent.setOnClickListener {
            Global.eventFoo.tryEmit(FooEvent("clicked in second"))
        }
        binding.fooFloat.setOnClickListener {
            Global.eventFloat.tryEmit(Random.nextFloat())
        }
        binding.fooString.setOnClickListener {
            Global.eventString.tryEmit("clicked in second")
        }

        binding.barEvent.setOnClickListener {
            EventBus.emit(BarEvent("clicked in second"))
        }
        binding.barFloat.setOnClickListener {
            EventBus.emit(Random.nextFloat())
        }
        binding.barString.setOnClickListener {
            EventBus.emit("clicked in second")
        }

        binding.second.visibility = View.GONE

    }

    private fun log(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("OoO", "$this => $message")
        binding.logs.text = "${binding.logs.text}\n$message"
    }
}