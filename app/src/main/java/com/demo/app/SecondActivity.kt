package com.demo.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.demo.app.databinding.ActivityMainBinding
import me.reezy.cosmo.flowbus.EventBus
import me.reezy.cosmo.flowbus.StateBus

class SecondActivity : AppCompatActivity() {

    val bus: EventBus by viewModels()

    private val binding by lazy { ActivityMainBinding.bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0)) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        StateBus.observe<Int>(this, "what") {
            binding.state.text = "state = $it"
        }

        binding.state.setOnClickListener {
            StateBus.update<Int>("what") {
                (it ?: 0) + 2
            }
        }


        EventBus.observe<FooEvent>(this) {
            log("event = $it")
        }

        binding.foo.setOnClickListener {
            EventBus.emit(FooEvent("clicked in second"))
        }
        binding.bar.setOnClickListener {
            EventBus.emit(BarEvent("clicked in second"))
        }
        binding.btnFloat.setOnClickListener {
            EventBus.emit(111f)
        }
        binding.fooString.setOnClickListener {
            EventBus.emitString("foo clicked in second")
        }

        binding.btnString.visibility = View.GONE
        binding.barString.visibility = View.GONE
        binding.second.visibility = View.GONE

    }

    private fun log(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e("OoO", message)
    }
}