package com.softbankrobotics.planning.utils

/**
 * An index regroups named objects with unique names.
 * Thread-safe.
 */
interface Index<T : Named> {
    /**
     * The set of all objects.
     */
    val all: Set<T>

    /**
     * Resolves the object by its name.
     */
    fun resolve(name: String): T?
}
