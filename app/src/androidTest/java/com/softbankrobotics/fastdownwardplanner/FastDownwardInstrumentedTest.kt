package com.softbankrobotics.fastdownwardplanner

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class FastDownwardInstrumentedTest {

    companion object {

        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun initContextAndPython() {
            // Context of the app under test.
            context = InstrumentationRegistry.getInstrumentation().targetContext
            Assert.assertEquals("com.softbankrobotics.fastdownwardplanner", context.packageName)
            initializePython(context)
        }

        fun stringFromRawResourceName(resourceName: String): String {
            val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
            return stringFromRawResource(context, resourceId)
        }
    }

    @Test
    fun translationToSAS1() {
        val domain = stringFromRawResourceName("fast_downward_1_domain")
        println("Domain:\n$domain")
        val problem = stringFromRawResourceName("fast_downward_1_problem")
        println("Problem:\n$problem")

        val sas = translatePDDLToSAS(domain, problem)
        Assert.assertEquals(stringFromRawResourceName("fast_downward_1_sas"), sas)
    }

    @Test
    fun searchPlanFromSAS1() {
        val sas = stringFromRawResourceName("fast_downward_1_sas")
        println("SAS:\n$sas")
        val plan = searchPlanFromSAS(sas,"astar(add())")
        println("Plan:\n$plan")
    }

    @Test
    fun searchPlanFromPDDL1() {
        val domain = stringFromRawResourceName("fast_downward_1_domain")
        println("Domain:\n$domain")
        val problem = stringFromRawResourceName("fast_downward_1_problem")
        println("Problem:\n$problem")
        val plan = searchPlan(domain, problem)
        println("Plan:\n$plan")
    }
}