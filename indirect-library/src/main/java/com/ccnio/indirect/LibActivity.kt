package com.ccnio.indirect

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "LibActivity"

class LibActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_lib2)
        R.string.common_indirect
        val string = resources.getString(R.string.common_indirect)
        Log.d(TAG, "onCreate: $string")
    }
}