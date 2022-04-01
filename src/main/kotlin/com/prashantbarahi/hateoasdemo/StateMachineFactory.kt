package com.prashantbarahi.hateoasdemo

import org.jboss.logging.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class StateMachineConfig<S : Enum<S>, E : Enum<E>> private constructor(
    val startState: S,
    val endState: S,
    val states: Set<S>
) {

    class Builder<S : Enum<S>, E : Enum<E>> {

        private lateinit var states: Set<S>
        private var startState: S? = null
        private var endState: S? = null

        fun withStartState(state: S): Builder<S, E> {
            this.startState = state
            return this
        }

        fun withEndState(state: S): Builder<S, E> {
            this.endState = state
            return this
        }

        fun withStates(states: Set<S>): Builder<S, E> {
            this.states = states
            return this
        }

        fun build(): StateMachineConfig<S, E> {
            require(states.isNotEmpty())
            require(endState != null)
            require(startState != null)
            require(states.contains(startState) && states.contains(endState))
            return StateMachineConfig(startState!!, endState!!, states)
        }

    }
}

class StateMachineFactory<S : Enum<S>, E : Enum<E>>(config: StateMachineConfig<S, E>) {


    private data class Node<S, E>(val state: S) {
        val edges = mutableMapOf<E, Node<S, E>>()
    }

    private val nodeRegistry = mutableMapOf<S, Node<S, E>>()
    private val endState: S
    private val startState: S
    private val machineRegistry = ConcurrentHashMap<Long, StateMachine<S, E>>()
    private var listener: ((prevState: S, event: E, nextState: S) -> Unit)? = null

    init {
        endState = config.endState
        startState = config.startState
        config.states.forEach {
            nodeRegistry[it] = Node(it)
        }
    }

    inner class StateMachine<S : Enum<S>, E : Enum<E>>
    constructor(private val factory: StateMachineFactory<S, E>, state: S) {
        private val logger = Logger.getLogger("StateMachine")

        private val _currentState: AtomicReference<S> = AtomicReference(state)
        val currentState: S
            get() {
                return _currentState.get()
            }


        fun getNextTransitions(): Set<E> {
            return factory.nodeRegistry[_currentState.get()]!!.edges.keys
        }

        fun sendEvent(event: E): Boolean {
            var isUpdated = false
            _currentState.getAndUpdate {
                val node = factory.nodeRegistry[_currentState.get()]!!
                val possibleEvents = node.edges.keys
                if (!possibleEvents.contains(event)) {
                    logger.error("Illegal event ${event.name}; current state is ${currentState.name}")
                    return@getAndUpdate it
                }
                val nextState = node.edges[event]!!.state
                factory.listener?.invoke(it, event, nextState)
                isUpdated = true
                nextState
            }
            return isUpdated
        }
    }

    fun addTransition(start: S, end: S, trigger: E) {
        require(start != endState)
        val startNode = nodeRegistry[start]!!
        startNode.edges[trigger] = nodeRegistry[end]!!
    }

    fun setOnTransitionListener(listener: (prevState: S, event: E, nextState: S) -> Unit) {
        this.listener = listener
    }

    fun create(id: Long): StateMachine<S, E> {
        return createFromState(id, startState)
    }

    fun createFromState(id: Long, state: S): StateMachine<S, E> {
        if (machineRegistry.contains(id)) {
            return machineRegistry[id]!!
        }
        val sm = StateMachine(this, state)
        machineRegistry[id] = sm
        return sm
    }
}

@Configuration
class Config {
    @Bean
    fun providesStateMachineFactory(): StateMachineFactory<ArticleState, ArticleEvent> {
        val config = StateMachineConfig.Builder<ArticleState, ArticleEvent>()
            .withStartState(ArticleState.DRAFT)
            .withEndState(ArticleState.PUBLISHED)
            .withStates(EnumSet.allOf(ArticleState::class.java))
            .build()

        val factory = StateMachineFactory(config)

        factory.addTransition(
            start = ArticleState.DRAFT,
            end = ArticleState.AUTHOR_SUBMITTED,
            trigger = ArticleEvent.AUTHOR_SUBMIT
        )
        factory.addTransition(
            start = ArticleState.AUTHOR_SUBMITTED,
            end = ArticleState.TE_APPROVED,
            trigger = ArticleEvent.TE_APPROVE
        )
        factory.addTransition(
            start = ArticleState.AUTHOR_SUBMITTED,
            end = ArticleState.DRAFT,
            trigger = ArticleEvent.TE_REJECT
        )
        factory.addTransition(
            start = ArticleState.TE_APPROVED,
            end = ArticleState.PUBLISHED,
            trigger = ArticleEvent.FPE_APPROVE
        )
        factory.addTransition(
            start = ArticleState.TE_APPROVED,
            end = ArticleState.DRAFT,
            trigger = ArticleEvent.FPE_REJECT
        )
        return factory
    }
}