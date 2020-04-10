package com.softbankrobotics.fastdownwardplanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        System.loadLibrary("native-lib");
        println(searchPlanFromSAS("ha", "ha"))
    }
}
