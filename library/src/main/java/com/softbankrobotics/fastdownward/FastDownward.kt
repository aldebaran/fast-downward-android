package com.softbankrobotics.fastdownward

import android.content.Context
import com.softbankrobotics.pddlplanning.*
import com.softbankrobotics.python.ensurePythonInitialized
import com.softbankrobotics.python.pythonIsInitialized

/**
 * Sets up the environment for the Fast Downward planner,
 * and provides the corresponding plan search function.
 */
fun setupFastDownwardPlanner(context: Context): PlanSearchFunction = synchronized(plannerLock) {
    ensurePythonInitialized(context)
    ensureFastDownwardIsLoaded()
    return ::searchPlanFastDownward
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
suspend fun searchPlanFastDownward(
    domain: String,
    problem: String,
    log: LogFunction? = null
): Tasks = synchronized(plannerLock) {
    if (!pythonIsInitialized)
        throw IllegalStateException("please initialize Python with `initializePython(context)` before using Fast Downward")
    ensureFastDownwardIsLoaded()

    if (log != null) log("Translating PDDL to SAS")
    val sas = try {
        translatePDDLToSAS(domain, problem)
    } catch (e: Throwable) {
        throw PDDLTranslationException(
            "Error in SAS translation: ${e.message}.\n" +
                    "PDDL Domain:\n$domain\n" +
                    "PDDL Problem:\n$problem"
        )
    }
    if (log != null) log("Translation done")

    val result = try {
        searchPlanFromSAS(sas, "astar(blind())")
    } catch (e: Throwable) {
        throw PDDLPlanningException(e.toString())
    }
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

/**
 * Whether the native library was already loaded.
 */
private var isFastDownwardLoaded = false

/**
 * Makes sure the native library is loaded.
 */
fun ensureFastDownwardIsLoaded() = synchronized(plannerLock) {
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
internal fun translatePDDLToSASInternal(domain: String, problem: String): String = synchronized(plannerLock) {
    ensureFastDownwardIsLoaded()
    return translatePDDLToSAS(domain, problem)
}

private external fun searchPlanFromSAS(sas: String, strategy: String): String
internal fun searchPlanFromSASInternal(sas: String, strategy: String): String = synchronized(plannerLock) {
    ensureFastDownwardIsLoaded()
    return searchPlanFromSAS(sas, strategy)
}

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