package com.ccnio.xplu

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ccnio.indirect.Indirect
import com.ccnio.indirect.LibActivity
import com.google.gson.Gson

//import com.ccnio.mylibrary.LibActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        R.layout.activity_common
        Gson()
//        LibActivity
        LibActivity::class.java
        findViewById<View>(R.id.btView).setOnClickListener {
            val intent = Intent("com.ccnio.LibActivity")
            startActivity(intent)
        }

//        Dest()

        R.string.direct
//        Direct() error

        R.string.indirect
        Indirect()

        R.string.common_indirect

    }
}