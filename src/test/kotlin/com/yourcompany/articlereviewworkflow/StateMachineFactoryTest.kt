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

package com.yourcompany.articlereviewworkflow

import com.yourcompany.articlereviewworkflow.statemachine.StateMachineConfigurer
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineFactory
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineKey
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent.*
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleState
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleState.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StateMachineFactoryTest {

  private lateinit var factory: StateMachineFactory<ArticleState, ArticleEvent>

  @BeforeEach
  fun setup() {
    val configuration = StateMachineConfigurer.StateBuilder<ArticleState, ArticleEvent>()
      .withStartState(DRAFT)
      .withEndState(PUBLISHED)
      .withStates(DRAFT, AUTHOR_SUBMITTED, TE_APPROVED, PUBLISHED)
      .and()
      .withTransitions {

        // Author
        defineTransition(start = DRAFT, end = AUTHOR_SUBMITTED, trigger = AUTHOR_SUBMIT)

        // TE
        defineTransition(start = AUTHOR_SUBMITTED, end = TE_APPROVED, trigger = TE_APPROVE)
        defineTransition(start = AUTHOR_SUBMITTED, end = DRAFT, trigger = TE_REJECT)

        // FPE
        defineTransition(start = TE_APPROVED, end = PUBLISHED, trigger = FPE_APPROVE)
        defineTransition(start = TE_APPROVED, end = DRAFT, trigger = FPE_REJECT)
      }
    factory = StateMachineFactory(object : StateMachineKey {
      override val key: String
        get() = ""

    }, configuration)
  }

  @Test
  fun shouldTransitionAsPerConfiguration() {
    val sm = factory.create()

    assertTrue(sm.currentState == DRAFT)
    assertTrue(sm.getNextTransitions().containsAll(listOf(AUTHOR_SUBMIT)))
    sm.sendEvent(AUTHOR_SUBMIT) shouldBe true

    assertTrue(sm.currentState == AUTHOR_SUBMITTED)
    assertTrue(sm.getNextTransitions().containsAll(listOf(TE_APPROVE, TE_REJECT)))

    sm.sendEvent(TE_APPROVE) shouldBe true
    assertTrue(sm.currentState == TE_APPROVED)
    assertTrue(sm.getNextTransitions().containsAll(listOf(FPE_APPROVE, FPE_REJECT)))

    sm.sendEvent(FPE_APPROVE) shouldBe true
    assertTrue(sm.currentState == PUBLISHED)
    assertTrue(sm.getNextTransitions().isEmpty())
  }

  @Test
  fun whenLoadedState_ShouldTransitionAsPerConfiguration() {
    val listOfEvents =
      listOf(AUTHOR_SUBMIT, TE_REJECT, AUTHOR_SUBMIT, TE_REJECT, AUTHOR_SUBMIT, TE_APPROVE)
    val sm = factory.buildFromHistory(listOfEvents)

    assertEquals(TE_APPROVED, sm.currentState)
    // wrong event
    sm.sendEvent(AUTHOR_SUBMIT)
    assertEquals(TE_APPROVED, sm.currentState)

    sm.sendEvent(FPE_APPROVE)
    assertEquals(PUBLISHED, sm.currentState)
  }

  @Test
  fun whenEventIsSent_stateShouldChangeIfTheEventIsConsumed() {
    val sm = factory.create()

    assertTrue(sm.currentState == DRAFT)
    assertTrue(sm.getNextTransitions().containsAll(listOf(AUTHOR_SUBMIT)))
    sm.sendEvent(AUTHOR_SUBMIT) shouldBe true
    assertTrue(sm.currentState == AUTHOR_SUBMITTED)

    sm.sendEvent(TE_APPROVE) shouldBe true
    assertTrue(sm.getNextTransitions().containsAll(listOf(FPE_APPROVE, FPE_REJECT)))
    sm.sendEvent(FPE_REJECT) shouldBe true
    assertTrue(sm.getNextTransitions().contains(AUTHOR_SUBMIT))

    sm.sendEvent(AUTHOR_SUBMIT) shouldBe true

    assertTrue(sm.currentState == AUTHOR_SUBMITTED)
    assertTrue(sm.getNextTransitions().containsAll(listOf(TE_APPROVE, TE_REJECT)))
    sm.sendEvent(TE_REJECT) shouldBe true
    sm.sendEvent(TE_REJECT) shouldBe false
    assertTrue(sm.currentState == DRAFT)
    sm.sendEvent(AUTHOR_SUBMIT) shouldBe true
    assertTrue(sm.currentState == AUTHOR_SUBMITTED)

  }

  infix fun <T> T.shouldBe(expected: T) {
    assertEquals(expected, this)
  }
}