package com.ccnio.mylibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class LibActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        R.array.int_arr
        R.color.blue_text_selector
    }
}