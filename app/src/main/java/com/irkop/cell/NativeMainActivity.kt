package com.irkop.cell

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/**
 * Legacy entry point kept as a compatibility shim.
 * The real native application entry point is MainActivity.
 */
class NativeMainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
