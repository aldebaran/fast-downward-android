package com.softbankrobotics.fastdownward

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test


class PDDLExamples {

    companion object {

        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun initContextAndPython() {
            // Context of the app under test.
            context = InstrumentationRegistry.getInstrumentation().targetContext
            Assert.assertEquals("com.softbankrobotics.fastdownward", context.packageName)
        }

        fun stringFromRawResourceName(resourceName: String): String {
            val resourceId =
                context.resources.getIdentifier(resourceName, "raw", context.packageName)
            return stringFromRawResource(context, resourceId)
        }

        fun searchPlanAndPrint(resourceName: String) {
            val pddl = stringFromRawResourceName(resourceName)
            val (domain, problem) = splitDomainAndProblem(pddl)
            val plan = searchPlan(domain, problem)
            println("Plan:\n$plan")
        }
    }

    @Test
    fun baseExample() {
        searchPlanAndPrint("example_base")
    }

    @Test
    fun aliceIsAlreadyHappy() {
        searchPlanAndPrint("example_alice_happy")
    }

    @Test
    fun everybodyIsHappy() {
        searchPlanAndPrint("example_alice_happy")
    }

    @Test
    fun nobodyAroundError() {
        searchPlanAndPrint("example_nobody_around_error")
    }

    @Test
    fun nobodyAroundConditionalGoal() {
        searchPlanAndPrint("example_nobody_around_conditional_goal")
    }

    @Test
    fun aliceIsAroundConditionalGoal() {
        searchPlanAndPrint("example_alice_around_conditional_goal")
    }

    @Test
    fun intrinsicAbsence() {
        searchPlanAndPrint("example_intrinsic_absence")
    }

    @Test
    fun nonDeterministic() {
        searchPlanAndPrint("example_non_deterministic")
    }

    @Test
    fun partialObservability() {
        searchPlanAndPrint("example_partial_observability")
    }

    @Test
    fun partialObservabilityNext() {
        searchPlanAndPrint("example_partial_observability_next")
    }

    @Test
    fun objectDiscoveryNobody() {
        searchPlanAndPrint("example_object_discovery_nobody")
    }

    @Test
    fun objectDiscovery() {
        searchPlanAndPrint("example_object_discovery")
    }

    @Test
    fun objectDiscoveryNext() {
        searchPlanAndPrint("example_object_discovery_next")
    }
}