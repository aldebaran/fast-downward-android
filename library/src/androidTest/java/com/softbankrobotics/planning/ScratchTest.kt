package com.softbankrobotics.planning

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.softbankrobotics.fastdownward.searchPlanFastDownward
import com.softbankrobotics.planning.utils.ensurePythonInitialized
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class ScratchTest {

    @Test
    fun planFromString() {
        val problem = "(define (problem sandbox_problem)\n" +
                "(:domain vms_domain)\n" +
                "\n" +
                "(:requirements :adl :negative-preconditions :universal-preconditions)\n" +
                "\n" +
                "(:objects\n" +
                "user - human;  Simple case: there is only one human, the user, and we reason around them\n" +
                ")\n" +
                "(:init\n" +
                "    (interacting_with user); A user entered interaction.\n" +
                "    (was_greeted user)\n" +
                ")\n" +
                "(:goal\n" +
                "(and\n" +
                "(imply (interacting_with user) (knows_intents user)); We check them in.\n" +
                "(imply (is_leaving user) (was_goodbyed user)); Good-bye users when they leave.\n" +
                "(forall (?i - intent); Make sure the user does not want anything (leads to satisfying their desires).\n" +
                "(not (wants user ?i))\n" +
                ")\n" +
                ")\n" +
                ")\n" +
                ")"
        val domain = "(define (domain vms_domain)\n" +
                "    \n" +
                "        (:requirements :adl :negative-preconditions :universal-preconditions)\n" +
                "    \n" +
                "        (:types human intent)\n" +
                "    \n" +
                "        (:constants\n" +
                "            show_menu get_coffee - intent)\n" +
                "    \n" +
                "        (:predicates\n" +
                "            (interacting_with ?h - human); Agent is interacting with the robot (mostly engaged, close to the robot).\n" +
                "            (is_leaving ?h - human); User is leaving\n" +
                "            (was_greeted ?h - human); Target has been greeted by the robot.\n" +
                "            (was_goodbyed ?h - human); Target has been goodbyed by the robot.\n" +
                "            (was_checked_in ?h - human); Target has been checked in by the robot.\n" +
                "            (wants ?h - human ?i - intent)\n" +
                "            (knows_intents ?h - human)\n" +
                "        )\n" +
                "    (:action greet\n" +
                "    :parameters (?h - human)\n" +
                "    :precondition (and (interacting_with ?h) (not (is_leaving ?h)))\n" +
                "    :effect (was_greeted ?h))\n" +
                "    (:action check_in\n" +
                "       :parameters (?h - human)\n" +
                "    :precondition (and (interacting_with ?h) (was_greeted ?h) (not (was_checked_in ?h)) (not (is_leaving ?h)))\n" +
                "    :effect (was_checked_in ?h)\n" +
                "    )\n" +
                "    (:action show_menu\n" +
                "       :parameters (?h - human)\n" +
                "    :precondition (and (interacting_with ?h) (was_checked_in ?h) (not (is_leaving ?h)))\n" +
                "    :effect (and (knows_intents ?h) (not (wants ?h show_menu))))\n" +
                "    \n" +
                "    )"
        val plan = searchPlanFastDownward(domain, problem)
        Assert.assertNotNull(plan)
        println("Plan found:\n${plan.joinToString()}")
        Assert.assertTrue(plan.isNotEmpty())
    }

    companion object {

        private lateinit var context: Context

        /**
         * Initializes the Android context and Python.
         */
        @BeforeClass
        @JvmStatic
        fun prepareContext() {
            // Context of the app under test.
            context = InstrumentationRegistry.getInstrumentation().targetContext
            ensurePythonInitialized(context)
        }
    }
}

