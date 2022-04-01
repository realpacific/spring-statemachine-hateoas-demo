package com.prashantbarahi.hateoasdemo.statemachine

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

class StateMachineFactory<S : Enum<S>, E : Enum<E>>
constructor(private val config: StateMachineStateConfigurer<S, E>.StateTransitionConfigurer) {

    interface OnStateTransitionListener<S, E> {
        fun onTransition(prevState: S, event: E, nextState: S)
    }

    inner class StateMachine(factory: StateMachineFactory<S, E>) {
        private var listener: OnStateTransitionListener<S, E>? = null

        private val logger = LoggerFactory.getLogger(StateMachine::class.java.simpleName)

        private val _currentNode: AtomicReference<Node<S, E>> = AtomicReference(factory.config.startNode)
        val currentState: S
            get() = _currentNode.get().state

        fun getNextTransitions(): Set<E> {
            return _currentNode.get().edges.keys
        }


        fun setOnTransitionListener(listener: OnStateTransitionListener<S, E>) {
            this.listener = listener
        }

        fun sendEvent(event: E): Boolean {
            var isUpdated = false
            var current: Node<S, E>? = null
            val newState = _currentNode.updateAndGet {
                current = it
                val possibleEvents = current!!.edges.keys
                if (!possibleEvents.contains(event)) {
                    return@updateAndGet it
                }
                val nextState = current!!.edges[event]
                nextState.apply { isUpdated = true }
            }
            if (!isUpdated) logger.error("Illegal event ${event.name}; current state is ${currentState.name}")
            else listener?.onTransition(current!!.state, event, newState.state)
            return isUpdated
        }
    }


    fun create(): StateMachine {
        return StateMachine(this)
    }

    fun buildFromHistory(events: List<E>): StateMachine {
        val sm = create()
        events.forEach {
            sm.sendEvent(it)
        }
        return sm
    }
}