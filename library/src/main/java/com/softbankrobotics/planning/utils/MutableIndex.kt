package com.softbankrobotics.planning.utils

/**
 * An index regroups named objects with unique names.
 * Thread-safe.
 */
class MutableIndex<T : Named> : Index<T> {
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
        val existing = mutableIndex[incoming.name]
        if (existing != null) {
            assertNoConflict(incoming, existing)
        } else {
            mutableIndex[incoming.name] = incoming
        }
    }

    /**
     * Removes the object from the index if not already absent.
     * @throws IllegalStateException If a different object is found under the same name.
     */
    fun remove(incoming: T) = synchronized(this) {
        val existing = mutableIndex[incoming.name]
        if (existing != null) {
            assertNoConflict(existing, incoming)
            mutableIndex.remove(incoming.name)
        }
    }

    companion object {
        /**
         * Checks that the two types do not have the same name while being different.
         * @throws IllegalStateException If the types are different but have the same name.
         */
        private fun assertNoConflict(existing: Named, incoming: Named) {
            if (existing.name == incoming.name) {
                if (existing != incoming) {
                    error("\"${existing.name}\" already exists but differs\n" +
                            "incoming: $incoming\nexisting: $existing")
                }
            }
        }
    }
}
