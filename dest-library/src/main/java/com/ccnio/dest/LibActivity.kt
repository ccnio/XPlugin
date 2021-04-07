package com.ccnio.dest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "LibActivity"

class LibActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        R.array.int_arr
        R.color.blue_text_selector
        R.string.common_indirect
        val string = resources.getString(R.string.common_indirect)
        Log.d(TAG, "onCreate: $string")
        resources.getStringArray(R.array.week)
    }
}