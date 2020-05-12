// IPlannerService.aidl
package com.softbankrobotics.fastdownwardplanner;

// Declare any non-default types here with import statements

interface IPlannerService {
    /**
     * Searches for a plan for the given PDDL description.
     * Returns a parsable plan in the following form:
     * task1 arg1 arg2\n
     * task2\n
     * task3 arg\n
     */
    String searchPlan(String pddl);
}
