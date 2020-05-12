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
    val result = searchPlan(domain, problem, "astar(add())")
    val (plan, _) = result.split(';')
    return plan.split("\n").map { line ->
        if (line.isEmpty())
            return@map null
        assert(line.startsWith('('))
        assert(line.endsWith(')'))
        line.substring(1, line.length-1)
    }.filterNotNull().joinToString("\n")
}