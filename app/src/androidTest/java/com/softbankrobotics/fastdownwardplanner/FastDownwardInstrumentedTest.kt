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

    @Test
    fun translateCrash1() {
        val domain = "(define (domain vms_domain)\n" +
                "    \n" +
                "        (:requirements :adl :negative-preconditions :universal-preconditions)\n" +
                "    \n" +
                "        (:types human intent good_vibe)\n" +
                "    \n" +
                "        (:constants\n" +
                "            enter_email show_menu get_coffee get_wifi_info get_company_info leave - intent)\n" +
                "    \n" +
                "        (:predicates\n" +
                "            (is_interested ?h - human)\n" +
                "            (can_be_engaged ?h - human)\n" +
                "            (engaged_with ?h - human)\n" +
                "            (is_leaving ?h - human); User is leaving\n" +
                "            (was_greeted ?h - human); Target has been greeted by the robot.\n" +
                "            (was_goodbyed ?h - human); Target has been goodbyed by the robot.\n" +
                "            (was_checked_in ?h - human); Target has been checked in by the robot.\n" +
                "            (does_not_have_qrcode ?h - human)\n" +
                "            (was_proposed ?h - human ?i - intent); We proposed to do something to the target.\n" +
                "            (was_consumed ?h - human ?i - intent); We know whether the target wants to do something.\n" +
                "            (wants ?h - human ?i - intent); The target wants to do something.\n" +
                "            (knows_intents ?h - human)\n" +
                "            (looking_alive)\n" +
                "            (provided_feedback ?h - human)\n" +
                "            (was_attracted ?h - human)\n" +
                "        )\n" +
                "    (:action attract\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (not (was_attracted ?h)) (not (is_leaving ?h)))\n" +
                "        :effect (and (was_attracted ?h) (can_be_engaged ?h) (engaged_with ?h))\n" +
                "    )\n" +
                "    \n" +
                "    (:action collect_feedback\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (was_checked_in ?h))\n" +
                "        :effect (provided_feedback ?h)\n" +
                "    )\n" +
                "    \n" +
                "    (:action idle\n" +
                "        :parameters ()\n" +
                "        :precondition ()\n" +
                "        :effect (looking_alive)\n" +
                "    )\n" +
                "    \n" +
                "    (:action enter_email\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (was_greeted ?h) (not (was_checked_in ?h)) (not (is_leaving ?h)) (does_not_have_qrcode ?h))\n" +
                "        :effect (was_checked_in ?h)\n" +
                "    )\n" +
                "    \n" +
                "    (:action goodbye\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (provided_feedback ?h)\n" +
                "        :effect (and (was_goodbyed ?h) (not (wants ?h leave)))\n" +
                "    )\n" +
                "    \n" +
                "    (:action greet\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (not (is_leaving ?h)))\n" +
                "        :effect (was_greeted ?h)\n" +
                "    )\n" +
                "    \n" +
                "    (:action make_coffee\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (was_checked_in ?h) (wants ?h get_coffee) (not (is_leaving ?h)))\n" +
                "        :effect (not (wants ?h get_coffee))\n" +
                "    )\n" +
                "    \n" +
                "    (:action present_company\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (was_checked_in ?h) (wants ?h get_company_info) (not (is_leaving ?h)))\n" +
                "        :effect (not (wants ?h get_company_info))\n" +
                "    )\n" +
                "    \n" +
                "    (:action scan\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (was_greeted ?h) (not (was_checked_in ?h)) (not (is_leaving ?h)) (not (does_not_have_qrcode ?h)))\n" +
                "        :effect (was_checked_in ?h)\n" +
                "    )\n" +
                "    \n" +
                "    (:action show_menu\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (was_checked_in ?h) (not (is_leaving ?h)))\n" +
                "        :effect (and (knows_intents ?h) (not (wants ?h show_menu)))\n" +
                "    )\n" +
                "    \n" +
                "    (:action show_wifi_info\n" +
                "        :parameters (?h - human)\n" +
                "        :precondition (and (engaged_with ?h) (was_checked_in ?h) (wants ?h get_wifi_info) (not (is_leaving ?h)))\n" +
                "        :effect (not (wants ?h get_wifi_info))\n" +
                "    )\n" +
                "    \n" +
                "    )"

        val problem = "(define (problem sandbox_problem)\n" +
                "        (:domain vms_domain)\n" +
                "    \n" +
                "        (:requirements :adl :negative-preconditions :universal-preconditions)\n" +
                "    \n" +
                "        (:objects\n" +
                "        )\n" +
                "    \n" +
                "        (:init\n" +
                "    (looking_alive))\n" +
                "    \n" +
                "        (:goal\n" +
                "        (and\n" +
                "        (imply (is_interested user) (knows_intents user))\n" +
                "        (imply (is_leaving user) (was_goodbyed user))\n" +
                "        (forall (?i - intent) (not (wants user ?i)))\n" +
                "        (imply (not (is_interested user)) (looking_alive))\n" +
                "        ))\n" +
                "    )"

        translatePDDLToSAS(domain, problem)
    }
}