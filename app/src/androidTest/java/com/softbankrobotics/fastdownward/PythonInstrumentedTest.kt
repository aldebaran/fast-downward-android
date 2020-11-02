package com.softbankrobotics.fastdownward

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PythonInstrumentedTest {

    companion object {
        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun contextAndPython() {
            // Context of the app under test.
            context = InstrumentationRegistry.getInstrumentation().targetContext
            assertEquals("com.softbankrobotics.fastdownward", context.packageName)
        }
    }

    @Test
    fun execHelloWorld() {
        val hello = "'Hello, world!'"
        val result = execPython(hello)
        assertEquals(hello, result)
    }

    @Test
    fun importBuiltinModule() {
        execPython("import sys")
        val result = execPython("sys")
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun importThirdPartyModule() {
        execPython("import translate")
        val result = execPython("translate")
        assertTrue(result.isNotEmpty())
    }
}
