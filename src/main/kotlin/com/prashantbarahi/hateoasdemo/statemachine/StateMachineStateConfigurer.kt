package com.prashantbarahi.hateoasdemo.statemachine

class StateMachineStateConfigurer<S : Enum<S>, E : Enum<E>>
private constructor(val startState: S, val endState: S, val states: Set<S>) {

    fun withTransitions(block: StateTransitionConfigurer.() -> StateTransitionConfigurer): StateTransitionConfigurer {
        return StateTransitionConfigurer().block()
    }

    inner class StateTransitionConfigurer {

        private val nodeRegistry: Map<S, Node<S, E>>
        val startNode: Node<S, E>
        val endNode: Node<S, E>

        init {
            val associationMap = mutableMapOf<S, Node<S, E>>()
            startNode = Node<S, E>(startState).apply { associationMap[state] = this }
            endNode = Node<S, E>(endState).apply { associationMap[state] = this }
            states.forEach { state ->
                if (!associationMap.containsKey(state)) associationMap[state] = Node(state)
            }
            nodeRegistry = associationMap
        }

        fun defineTransition(start: S, end: S, trigger: E): StateTransitionConfigurer {
            require(start != endState)
            val startNode = nodeRegistry[start]!!
            startNode.edges[trigger] = nodeRegistry[end]!!
            return this
        }
    }

    class StateBuilder<S : Enum<S>, E : Enum<E>> {

        private lateinit var states: Set<S>
        private var startState: S? = null
        private var endState: S? = null

        fun withStartState(state: S): StateBuilder<S, E> {
            this.startState = state
            return this
        }

        fun withEndState(state: S): StateBuilder<S, E> {
            this.endState = state
            return this
        }

        fun withStates(states: Set<S>): StateBuilder<S, E> {
            this.states = states
            return this
        }

        fun withStates(vararg states: S): StateBuilder<S, E> {
            this.states = states.toSet()
            return this
        }

        fun and(): StateMachineStateConfigurer<S, E> {
            require(states.isNotEmpty())
            require(endState != null)
            require(startState != null)
            require(states.contains(startState) && states.contains(endState))
            return StateMachineStateConfigurer(startState!!, endState!!, states)
        }
    }
}
