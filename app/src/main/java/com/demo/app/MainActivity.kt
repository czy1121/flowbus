package com.demo.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.demo.app.databinding.ActivityMainBinding
import me.reezy.cosmo.flowbus.BusKey
import me.reezy.cosmo.flowbus.EventBus
import me.reezy.cosmo.flowbus.StateBus
import me.reezy.cosmo.flowbus.busKey

class MainActivity : AppCompatActivity() {

    val bus: EventBus by viewModels()

    private val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }


    inline fun <reified T> event(name: String) = name to T::class.java


    private val EVENT_PUNCH = busKey<Int>("punch")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.observe(this, EVENT_PUNCH) {

        }

        StateBus.set("what", 1)

        StateBus.observe<Int>(this, "what") {
            binding.state.text = "state = $it"
        }

        EventBus.observe<Float>(this) {
            log("main.observe<Float> = $it")
        }

        EventBus.observe<FooEvent>(this) {
            log("main => FooEvent(value = ${it.value})")
        }
        EventBus.observe<BarEvent>(this) {
            log("main => BarEvent(value = ${it.value})")
        }

        EventBus.observe<String>(this) {
            log("main.observe<String> = $it")
        }

        EventBus.observe<String>(this,"foo") {
            log("main.observe<String>(foo) = $it")
        }

        EventBus.observe<String>(this,"bar") {
            log("main.observe<String>(bar) = $it")
        }

        bus.observe<String>(this,"foo") {

        }

        binding.state.setOnClickListener {
            StateBus.update<Int>("what") {
                (it ?: 0) + 1
            }
        }
        binding.foo.setOnClickListener {
            EventBus.emit(FooEvent("clicked in main"))
        }
        binding.bar.setOnClickListener {
            EventBus.emit(BarEvent("clicked in main"))
        }
        binding.btnFloat.setOnClickListener {
            EventBus.emit(111f)
        }
        binding.btnString.setOnClickListener {
            EventBus.emit("string 111 clicked")
        }
        binding.fooString.setOnClickListener {
            EventBus.emitString("foo clicked in main")
        }
        binding.barString.setOnClickListener {
            EventBus.emitString("bar clicked in main")
        }

        binding.second.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

    }

    private fun log(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("OoO", "$this => $message")
    }
}