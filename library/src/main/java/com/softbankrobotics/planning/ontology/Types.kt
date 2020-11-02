package com.softbankrobotics.planning.ontology

import java.util.*
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

interface TypeDeclaration {
    val type: Type
}

/** Reflexive type to refer to PDDL predicates. */
class Predicate(name: String) : Instance(name) {
    companion object: TypeDeclaration {
        override val type = Type("predicate")
    }

    override val type: Type = Companion.type
}

/** Something that has a physical presence in the world. */
open class PhysicalObject(name: String) : Instance(name) {
    companion object : TypeDeclaration {
        override val type = Type("physical_object")
    }

    override val type: Type = Companion.type
}

/** Something that can believe, desire, intend. */
open class AgentivePhysicalObject(name: String) : PhysicalObject(name) {
    companion object {
        val type = Type("social_agent", PhysicalObject.type)
    }

    override val type: Type = Companion.type
}

/** A human as the robot can see them. */
class Human(name: String) : AgentivePhysicalObject(name) {
    companion object : TypeDeclaration {
        override val type = Type("human", AgentivePhysicalObject.type)
    }

    override val type: Type = Companion.type
}

val humanType = Human.type

/** An intent that can be desired by a social agent. */
class Intent(name: String) : Instance(name) {
    companion object : TypeDeclaration {
        override val type = Type("intent")
    }

    override val type: Type? = Companion.type
}

/** A point of interest in space. */
class Poi(name: String) : PhysicalObject(name) {
    companion object : TypeDeclaration {
        override val type = Type("poi", PhysicalObject.type)
    }

    override val type: Type = Companion.type
}

val poiType = Poi.type

// TODO: merge this with "predicate"
/** A symbolic statement that can be known. */
class Statement(name: String) : Instance(name) {
    companion object : TypeDeclaration {
        override val type = Type("statement")
    }

    override val type: Type = Companion.type
}

/** A reification of an action. */
class ReifiedAction(name: String) : Instance(name) {
    companion object : TypeDeclaration {
        override val type = Type("reified_action") // TODO: try with name "action"

        /** Shortcut to create a new symbol reifying the given PDDL action. */
        fun of(pddl: Action): ReifiedAction {
            return ReifiedAction("${pddl.name}_action")
        }
    }

    override val type: Type = Companion.type
}

/**
 * Create an instance of the right Kotlin type given the PDDL Type.
 */
fun createInstance(
    name: String,
    typeName: String
): Instance {
    return when (typeName) {
        Predicate.type.name -> Predicate(name)
        AgentivePhysicalObject.type.name -> AgentivePhysicalObject(name)
        Human.type.name -> Human(name)
        Intent.type.name -> Intent(name)
        Poi.type.name -> Poi(name)
        Statement.type.name -> Statement(name)
        ReifiedAction.type.name -> ReifiedAction(name)
        else -> throw RuntimeException("Unknown type $typeName")
    }
}

// TODO: should be provided in the core of the ontology, and support extension.
/**
 * Create an instance of the right Kotlin type given the PDDL Type.
 */
inline fun <reified T : Instance> createInstance(name: String): T {
    return T::class.primaryConstructor!!.call(name)
}

/**
 * Generates a random PDDL object of any instance type.
 */
inline fun <reified T : Instance> generateInstance(): T {
    val id = Random().nextInt(99999)
    val strId = id.toString().padStart(5, '0')
    val companion = T::class.companionObjectInstance!! as TypeDeclaration
    return createInstance("${companion.type.name}_$strId")
}
