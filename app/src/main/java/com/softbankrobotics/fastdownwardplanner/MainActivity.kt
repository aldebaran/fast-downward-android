package com.softbankrobotics.fastdownwardplanner

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun stringFromRawResource(context: Context, resourceId: Int): String {
    val inputStream = context.resources.openRawResource(resourceId)
    return Scanner(inputStream, "UTF-8").useDelimiter("\\A").next()
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        System.loadLibrary("native-lib")
        initializePython(this)
        val domain = stringFromRawResource(this, R.raw.fast_downward_1_domain)
        val problem = stringFromRawResource(this, R.raw.fast_downward_1_problem)
        println(searchPlan(domain, problem))
    }
}
