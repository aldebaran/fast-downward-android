package com.softbankrobotics.fastdownward

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.softbankrobotics.pddlplanning.IPDDLPlannerService
import com.softbankrobotics.pddlplanning.IPDDLPlannerService.ACTION_SEARCH_PLANS_FROM_PDDL
import com.softbankrobotics.pddlplanning.PDDLPlanningException
import com.softbankrobotics.pddlplanning.PDDLTranslationException
import com.softbankrobotics.pddlplanning.Task
import kotlinx.coroutines.runBlocking

class PlannerService : Service() {

    private val planSearchFunction by lazy {
        try {
            setupFastDownwardPlanner(this)
        } catch (e: Throwable) {
            throw IllegalStateException("Fast Downward could not be initialized: $e")
        }
    }

    private val binder = object : IPDDLPlannerService.Stub() {

        override fun searchPlan(domain: String, problem: String): List<Task> {
            Log.i(TAG, "Searching plan for PDDL:\n$domain\n$problem")
            return try {
                runBlocking { planSearchFunction(domain, problem) { Log.i(TAG, it) } }
            } catch (e: PDDLTranslationException) {
                throw IllegalArgumentException(e.message ?: "unknown error in input PDDL")
            } catch (e: PDDLPlanningException) {
                throw UnsupportedOperationException(e.message ?: "unknown error in PDDL planning")
            } catch (e: Throwable) {
                throw e
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        if (intent.action == ACTION_SEARCH_PLANS_FROM_PDDL) {
            Log.i(TAG, "Service is being bound")
            return binder
        } else {
            Log.w(TAG, "Unsupported intent action was used to start the service: ${intent.action}")
            throw RuntimeException("Unsupported intent action was used to start the service: ${intent.action}")
        }
    }

    companion object {
        const val TAG = "FastDownward"
    }
}
