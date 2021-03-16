package com.ccnio.xplu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ccnio.mylibrary.LibActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LibActivity::class.java
    }
}