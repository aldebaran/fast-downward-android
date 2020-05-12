package com.softbankrobotics.fastdownwardplanner

import android.app.Service
import android.content.Intent
import android.os.IBinder

const val ACTION_SEARCH_PLANS_FROM_PDDL = "com.softbankrobotics.planning.action.SEARCH_PLANS_FROM_PDDL"

class PlannerService : Service() {

    private val binder = object : IPlannerService.Stub() {

        override fun searchPlan(pddl: String): String {
            println("Searching plan for PDDL:\n$pddl")
            val domain = pddl.substringBeforeLast("(define ")
            val problem = pddl.substring(domain.length)
            val plan = searchPlan(domain, problem)
            return plan
        }
    }

    override fun onBind(intent: Intent): IBinder {
        println("Service is being bound")
        assert(intent.action == ACTION_SEARCH_PLANS_FROM_PDDL)
        return binder
    }
}
