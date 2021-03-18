package com.ccnio.xplu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ccnio.mylibrary.LibActivity
import com.google.gson.Gson

//import com.ccnio.mylibrary.LibActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Gson()
//        LibActivity
        LibActivity::class.java
        findViewById<View>(R.id.btView).setOnClickListener {
            val intent = Intent("com.ccnio.LibActivity")
            startActivity(intent)
        }
    }
}