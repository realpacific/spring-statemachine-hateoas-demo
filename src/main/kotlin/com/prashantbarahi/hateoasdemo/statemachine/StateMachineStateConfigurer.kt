package com.prashantbarahi.hateoasdemo.statemachine

/**
 * Configure state machine with the states and define transitions between those states
 */
class StateMachineStateConfigurer<S : Enum<S>, E : Enum<E>>
private constructor(val startState: S, val endState: S, val states: Set<S>) {

    fun withTransitions(block: StateTransitionConfigurer.() -> StateTransitionConfigurer): StateTransitionConfigurer {
        return StateTransitionConfigurer().block()
    }

    /**
     * Define transitions between states [S] of the [StateMachineFactory.StateMachine] given events [E]
     *
     * @property startNode [Node] for [startState]
     * @property endNode [Node] for [endState]
     */
    inner class StateTransitionConfigurer {

        private val nodeRegistry: Map<S, Node<S, E>> // immutable Map
        val startNode: Node<S, E>
        val endNode: Node<S, E>

        init {
            val associationMap = mutableMapOf<S, Node<S, E>>() // temporary association holder
            startNode = Node<S, E>(startState).apply { associationMap[state] = this }
            endNode = Node<S, E>(endState).apply { associationMap[state] = this }
            states.forEach { state ->
                if (!associationMap.containsKey(state)) associationMap[state] = Node(state)
            }
            nodeRegistry = associationMap
        }

        /**
         * Define trigger [trigger] that transitions state machine from state [start] to state [end]
         */
        fun defineTransition(start: S, end: S, trigger: E): StateTransitionConfigurer {
            require(start != endState)
            val startNode = nodeRegistry[start]!!
            startNode.edges[trigger] = nodeRegistry[end]!! // connect the two states
            return this
        }
    }

    /**
     * Define all possible states [S] of a [StateMachineFactory.StateMachine]
     */
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
            return withStates(states.toSet())
        }

        /**
         * Initializes [StateMachineStateConfigurer] with all the states
         *
         * @return [StateMachineStateConfigurer]
         */
        fun and(): StateMachineStateConfigurer<S, E> {
            require(states.isNotEmpty())
            requireNotNull(endState)
            requireNotNull(startState)
            return StateMachineStateConfigurer(startState!!, endState!!, states)
        }
    }
}
