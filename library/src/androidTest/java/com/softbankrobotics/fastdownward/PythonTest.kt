package com.softbankrobotics.fastdownward

import androidx.test.platform.app.InstrumentationRegistry
import com.softbankrobotics.python.ensurePythonInitialized
import org.junit.Assert.assertFalse
import org.junit.Test

class PythonTest {
    @Test
    fun initialization() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val pythonPath = ensurePythonInitialized(context)
        assertFalse(pythonPath.isEmpty())
    }
}
