package com.softbankrobotics.planning.utils

import android.content.Context
import com.softbankrobotics.python.initializePython
import com.softbankrobotics.python.pythonIsInitialized
import com.softbankrobotics.python.pythonSysPath

/**
 * Helper for tests that cannot tell if they are run along with other tests.
 * If you call this from production code, you are probably doing it wrong.
 */
fun ensurePythonInitialized(context: Context): String {
    return if (pythonIsInitialized)
        pythonSysPath
    else
        initializePython(context)
}