package com.softbankrobotics.fastdownwardplanner

import android.app.Application

class PlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("native-lib")
        val pythonPath = initializePython(this)
        println("Python initialized with path: $pythonPath")
    }
}