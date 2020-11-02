package com.softbankrobotics.planning

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.softbankrobotics.fastdownward.searchPlanFastDownward
import com.softbankrobotics.fastdownward.translatePDDLToSASInternal
import com.softbankrobotics.planning.ontology.*
import com.softbankrobotics.planning.utils.ensurePythonInitialized
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

typealias ExpressionToTask = Pair<Collection<Expression>, Tasks>
typealias ExpressionsToTasks = List<Pair<Collection<Expression>, Tasks>>

/**
 * Checking that planning works.
 */
class PlanningTest {

    @Test
    fun plannerSanityCheck() {
        val (domain, problem) = domainAndProblemFromRaw(context, R.raw.simple)
        val expectedPlan = listOf(Task("ask"))
        val plan = searchPlanFastDownward(domain, problem)
        assertEquals(expectedPlan, plan)
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

        translatePDDLToSASInternal(domain, problem)
    }

    @Test
    fun planDraftVMS() {
        val (domain, problem) = domainAndProblemFromRaw(context, R.raw.draft_vms)
        val initsToPlans = mutableListOf<ExpressionToTask>()

        // With PDDL4J, only AStar + SUM works.
        initsToPlans.add(
            listOf<Expression>() // Nothing to say about user.
            to listOf() // Robot has nothing to do.
        )

        // Did manage to make it not work in PDDL4J.
        initsToPlans.add(
            listOf( // A user entered interaction.
                createFact("interacting_with", "user")
            ) to listOf( // Robots checks them in.
                Task.create("greet", "user"),
                Task.create("check_in", "user")
            )
        )

        // Fast-downward works fine for all these problems.
        checkPlansForInits(domain, problem, initsToPlans)
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

        private fun searchPlanForInit(domain: String, problem: String, init: Facts): Tasks {
            val problemWithInit = replaceInit(problem, init)
            println("Searching plan for init:\n${init.joinToString("\n")}")
            val plan =
                searchPlanFastDownward(domain, problemWithInit)
            println("Found plan:\n${plan.joinToString("\n")}")
            return plan
        }

        /**
         * Checks whether the right plan is found for a given init in a problem.
         * Note that it replaces the init of the problem.
         */
        private fun checkPlanForInit(domain: String, problem: String, init: Collection<Expression>, expectedPlan: Tasks) {
            val plan = searchPlanForInit(domain, problem, init)
            assertEquals(expectedPlan, plan)
        }

        /**
         * Checks whether the right plans are found for given inits in a problem.
         * Note that it replaces the init of the problem all along.
         * The problem ends up with the last init.
         */
        private fun checkPlansForInits(
            domain: String, problem: String,
            initsToExpectedPlans: ExpressionsToTasks) {
            initsToExpectedPlans.forEach { initToExpectedPlan ->
                checkPlanForInit(domain, problem, initToExpectedPlan.first, initToExpectedPlan.second)
            }
        }

        private fun stringFromRaw(context: Context, id: Int): String {
            val input = context.resources.openRawResource(id)
            return String(input.readBytes(), Charsets.UTF_8)
        }

        /**
         * Reads PDDL domain and problem from a raw resource, given its identifier.
         */
        fun domainAndProblemFromRaw(context: Context, id: Int): Pair<String, String> {
            val pddl = stringFromRaw(context, id)
            println("Using base PDDL: $pddl")
            return splitDomainAndProblem(pddl)
        }
    }
}
