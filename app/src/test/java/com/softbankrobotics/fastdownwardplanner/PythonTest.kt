package com.softbankrobotics.fastdownwardplanner

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.python.util.PythonInterpreter

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class PythonTest {

    private lateinit var python: PythonInterpreter

    @Before
    fun createPythonInterpreter() {
        python = PythonInterpreter()
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
