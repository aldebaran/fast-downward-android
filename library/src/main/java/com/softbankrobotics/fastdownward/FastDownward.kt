package com.softbankrobotics.fastdownward

import com.softbankrobotics.planning.LogFunction
import com.softbankrobotics.planning.ontology.Task
import com.softbankrobotics.planning.ontology.Tasks
import com.softbankrobotics.python.pythonIsInitialized

/**
 * Search for a plan using fast downward planner and default search strategy.
 * Python must be initialized first. Please call initializePython(context) to do so.
 */
fun searchPlanFastDownward(domain: String, problem: String): Tasks {
    return searchPlanFastDownward(domain, problem, null)
}

/**
 * Search for a plan using default planner and search strategies.
 * Python must be initialized first. Please call initializePython(context) to do so.
 * Accepts a log function optionally.
 * @param domain A PDDL domain.
 * @param problem A PDDL problem.
 * @param log A function to log details.
 * @return A list of tasks to perform.
 * @throws RuntimeException If the problem is not well formed, or if there is no solution.
 */
fun searchPlanFastDownward(
    domain: String,
    problem: String,
    log: LogFunction? = null
): Tasks =
    synchronized(plannerLock) {
        if (!pythonIsInitialized)
            throw RuntimeException("please initialize Python with `initializePython(context)` before using Fast Downward")
        ensureFastDownwardIsLoaded()

        if (log != null) log("Translating PDDL to SAS")
        val sas = try {
            translatePDDLToSAS(domain, problem)
        } catch (t: Throwable) {
            throw RuntimeException(
                "Error in SAS translation: ${t.message}.\n" +
                        "PDDL Domain:\n$domain\n" +
                        "PDDL Problem:\n$problem"
            )
        }
        if (log != null) log("Translation done")

        val result = searchPlanFromSAS(sas, "astar(blind())")
        if (log != null) log("Raw planner result: $result")
        val (plan, _) = result.split(';')
        return plan.split("\n").map { line ->
            if (line.isEmpty())
                return@map null
            if (!line.startsWith('(') || !line.endsWith(')')) {
                error("Malformed line of plan result:\n$line\nIn:\n$result")
            }
            val task = line.substring(1, line.length - 1)
            val taskArgs = task.split(' ').filter { it.isNotEmpty() }.map { it.trim() }
            Task.create(*taskArgs.toTypedArray())
        }.filterNotNull()
    }

/** Search plan with default strategy: astar(blind()). */
fun searchPlan(domain: String, problem: String): String {
    ensureFastDownwardIsLoaded()
    val result = searchPlanFastDownward(domain, problem, "astar(blind())")
    val (plan, _) = result.split(';')
    return plan.split("\n").map { line ->
        if (line.isEmpty())
            return@map null
        assert(line.startsWith('('))
        assert(line.endsWith(')'))
        line.substring(1, line.length - 1)
    }.filterNotNull().joinToString("\n")
}

/** Splits PDDL content into a domain and a problem. */
fun splitDomainAndProblem(pddlContent: String): Pair<String, String> {
    val domain = pddlContent.substringBeforeLast("(define ")
    val problem = pddlContent.substring(domain.length)
    return Pair(domain, problem)
}

/**
 * Whether the native library was already loaded.
 */
private var isFastDownwardLoaded = false

/**
 * Makes sure the native library is loaded.
 */
fun ensureFastDownwardIsLoaded() {
    if (!isFastDownwardLoaded) {
        System.loadLibrary("fast-downward-jni")
        isFastDownwardLoaded = true
    }
}

/**
 * Translates the provided PDDL problem into an SAS string.
 * Requires initializePython to be called beforehand.
 */
private external fun translatePDDLToSAS(domain: String, problem: String): String
internal fun translatePDDLToSASInternal(domain: String, problem: String): String {
    ensureFastDownwardIsLoaded()
    return translatePDDLToSAS(domain, problem)
}

private external fun searchPlanFromSAS(sas: String, strategy: String): String

/**
 * Search plan using Fast Downward.
 * It uses Python, and requires Python to be initialized first.
 * @param domain Domain defined as a PDDL string.
 * @param problem Problem defined as a PDDL string.
 * @param strategy Strategy to use, as accepted by fast-downward: http://www.fast-downward.org/PlannerUsage
 * @return A plan as output by fast-downward.
 */
external fun searchPlanFastDownward(
    domain: String,
    problem: String,
    strategy: String
): String

/**
 * The library is not thread safe.
 * This object is used to synchronize calls.
 */
private val plannerLock = Any()