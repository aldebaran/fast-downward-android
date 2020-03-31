package com.softbankrobotics.fastdownwardplanner

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.python.core.Options
import org.python.core.PySystemState
import org.python.util.PythonInterpreter

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PythonInstrumentedTest {

    @Before
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.softbankrobotics.fastdownwardplanner", appContext.packageName)
    }

    private lateinit var python: PythonInterpreter

    @Before
    fun createPythonInterpreter() {
        val pythonConfig = PySystemState()
        Options.importSite = false
        python = PythonInterpreter(null, pythonConfig)
    }

    @Test
    fun canRunPythonHelloWorld() {
        python.exec("print('Hello Python World!')")
        python.exec("import sys")
        python.exec("print(sys.path)")
    }

    @Test
    fun canFindFastDownwardTranslate() {
        python.exec("import translate")
    }
}
