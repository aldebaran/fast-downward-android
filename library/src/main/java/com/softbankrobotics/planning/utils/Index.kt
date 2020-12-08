package com.softbankrobotics.planning.utils

/**
 * An index regroups named objects with unique string identifiers (aka names).
 * Thread-safe.
 */
interface Index<T> {
    /**
     * The set of all objects.
     */
    val all: Set<T>

    /**
     * Resolves the object by its name.
     */
    fun resolve(name: String): T?
}

/**
 * An index regroups named objects with unique names.
 * Thread-safe.
 */
class MutableIndex<T>(private val nameOf: (T) -> String) : Index<T> {

    private val mutableIndex = mutableMapOf<String, T>()

    override val all: Set<T> get() = synchronized(this) {
        mutableIndex.values.toSet()
    }

    override fun resolve(name: String): T? = synchronized(this) {
        return mutableIndex[name]
    }

    /**
     * Adds the object to the index if not already found.
     * @throws IllegalStateException If a different object is found under the same name.
     */
    fun ensure(incoming: T) = synchronized(this) {
        val existing = mutableIndex[nameOf(incoming)]
        if (existing != null) {
            assertNoConflict(incoming, existing, nameOf)
        } else {
            mutableIndex[nameOf(incoming)] = incoming
        }
    }

    /**
     * Removes the object from the index if not already absent.
     * @throws IllegalStateException If a different object is found under the same name.
     */
    fun remove(incoming: T) = synchronized(this) {
        val existing = mutableIndex[nameOf(incoming)]
        if (existing != null) {
            assertNoConflict(existing, incoming, nameOf)
            mutableIndex.remove(nameOf(incoming))
        }
    }

    companion object {
        /**
         * Checks that the two types do not have the same name while being different.
         * @throws IllegalStateException If the types are different but have the same name.
         */
        private fun <T> assertNoConflict(existing: T, incoming: T, nameOf: (T) -> String) {
            if (nameOf(existing) == nameOf(incoming)) {
                if (existing != incoming) {
                    error("\"${nameOf(existing)}\" already exists but differs\n" +
                            "incoming: $incoming\nexisting: $existing")
                }
            }
        }
    }
}

fun <T : Named> createMutableIndex(): MutableIndex<T> = MutableIndex { it.name }
fun <T> createMutableIndex(nameOf: (T) -> String): MutableIndex<T> = MutableIndex(nameOf)