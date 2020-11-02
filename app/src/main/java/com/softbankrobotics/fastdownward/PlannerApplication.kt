package com.softbankrobotics.fastdownward

import android.app.Application
import com.softbankrobotics.python.initializePython

class PlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val pythonPath = initializePython(this)
        println("Python initialized with path: $pythonPath")
    }
}