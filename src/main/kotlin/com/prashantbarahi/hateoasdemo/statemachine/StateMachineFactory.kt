package com.prashantbarahi.hateoasdemo.statemachine

import com.prashantbarahi.hateoasdemo.statemachine.StateMachineFactory.StateMachine
import org.slf4j.LoggerFactory
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicReference

/**
 * Build a [StateMachineFactory] using a [config]
 * such that [create] returns a new [StateMachine] that's built from that [config]
 */
class StateMachineFactory<S : Enum<S>, E : Enum<E>>
constructor(
    val identifier: StateMachineKey,
    private val config: StateMachineConfigurer<S, E>.StateTransitionConfigurer
) {
  private val logger = LoggerFactory.getLogger(StateMachineFactory::class.java.simpleName)

  interface OnStateTransitionListener<S, E> {
    fun onTransition(prevState: S, event: E, nextState: S)
  }

  private val endState: S = config.endNode.state

  inner class StateMachine {
    private var listener: OnStateTransitionListener<S, E>? = null

    private val _currentNode: AtomicReference<Node<S, E>> = AtomicReference(config.startNode)
    val currentState: S
      get() = _currentNode.get().state

    /**
     * @return the events that [StateMachine] can consume
     */
    fun getNextTransitions(): Set<E> {
      return _currentNode.get().edges.keys
    }

    fun isCompleted() = _currentNode.get().state == endState

    fun setOnTransitionListener(listener: OnStateTransitionListener<S, E>) {
      this.listener = listener
    }

    /**
     * *Attempts* to transition from the current state of a [StateMachine] to the next state, given a trigger [event]
     *
     * @return `true` if the transition was successful; else `false`
     */
    fun sendEvent(event: E): Boolean {
      if (isCompleted()) return false
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


  /**
   * @return [StateMachine] that is built using [config]
   */
  fun create(): StateMachine {
    return StateMachine()
  }

  /**
   * Restores the state of a new instance of [StateMachine] by iterating through [events]
   * and calling the [StateMachine.sendEvent]
   */
  fun buildFromHistory(events: List<E>): StateMachine {
    val sm = create()
    events.forEach {
      if (!sm.sendEvent(it)) {
        throw IllegalStateException("Error processing event $it")
      }
    }
    return sm
  }
}