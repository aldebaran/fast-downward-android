package com.softbankrobotics.planning.ontology

import com.softbankrobotics.planning.ontology.Instance
import com.softbankrobotics.planning.ontology.Type

/** Reflexive type to refer to PDDL predicates. */
class Predicate(name: String) : Instance(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        override val type = Type("predicate", null) { Predicate(it) }
    }
}

/** Something that has a physical presence in the world. */
open class PhysicalObject(name: String) : Instance(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        override val type = Type("physical_object", null) { PhysicalObject(it) }
    }
}

/** Something that can believe, desire, intend. */
open class AgentivePhysicalObject(name: String) : PhysicalObject(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        override val type = Type("social_agent", PhysicalObject.type) { AgentivePhysicalObject(it) }
    }
}

/** A human as the robot can see them. */
class Human(name: String) : AgentivePhysicalObject(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        override val type = Type("human", AgentivePhysicalObject.type) { Human(it) }
    }
}

val humanType = Human.type

/** An intent that can be desired by a social agent. */
class Intent(name: String) : Instance(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        override val type = Type("intent", null) { Intent(it) }
    }
}

/** A point of interest in space. */
class Poi(name: String) : PhysicalObject(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        override val type = Type("poi", PhysicalObject.type) { Poi(it) }
    }
}

val poiType = Poi.type

// TODO: merge this with "predicate"
/** A symbolic statement that can be known. */
class Statement(name: String) : Instance(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        override val type = Type("statement", null) { Statement(it) }
    }
}

/** A reification of an action. */
class ReifiedAction(name: String) : Instance(name) {
    override val type: Type = Companion.type

    companion object : Typed {
        // TODO: try with name "action"
        override val type = Type("reified_action", null) { ReifiedAction(it) }

        /** Shortcut to create a new symbol reifying the given PDDL action. */
        fun of(pddl: Action): ReifiedAction {
            return ReifiedAction("${pddl.name}_action")
        }
    }
}

