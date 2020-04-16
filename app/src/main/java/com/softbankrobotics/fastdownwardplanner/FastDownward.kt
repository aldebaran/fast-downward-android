package com.softbankrobotics.fastdownwardplanner

/**
 * Translates the provided PDDL problem into an SAS string.
 * Requires initializePython to be called beforehand.
 */
external fun translatePDDLToSAS(domain: String, problem: String): String
external fun searchPlanFromSAS(sas: String, strategy: String): String
external fun searchPlan(domain: String, problem: String, strategy: String): String

/** Search plan with default strategy: astar(add()). */
fun searchPlan(domain: String, problem: String): String {
    return searchPlan(domain, problem, "astar(add())")
}