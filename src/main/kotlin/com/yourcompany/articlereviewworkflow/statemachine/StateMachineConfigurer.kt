/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yourcompany.articlereviewworkflow.statemachine

/**
 * Configure state machine with the states and define transitions between those states
 */
class StateMachineConfigurer<S : Enum<S>, E : Enum<E>>
private constructor(val startState: S, val endState: S, val states: Set<S>) {

  fun withTransitions(
    block: StateTransitionConfigurer.() -> StateTransitionConfigurer
  ): StateTransitionConfigurer {
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
   * Define all the possible states [S] of a [StateMachineFactory.StateMachine]
   */
  class StateBuilder<S : Enum<S>, E : Enum<E>> {

    private lateinit var states: Set<S>
    var startState: S? = null
      private set
    var endState: S? = null
      private set

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
     * Initializes [StateMachineConfigurer] with all the states
     *
     * @return [StateMachineConfigurer]
     */
    fun and(): StateMachineConfigurer<S, E> {
      require(states.isNotEmpty())
      requireNotNull(endState)
      requireNotNull(startState)
      return StateMachineConfigurer(startState!!, endState!!, states)
    }
  }
}
