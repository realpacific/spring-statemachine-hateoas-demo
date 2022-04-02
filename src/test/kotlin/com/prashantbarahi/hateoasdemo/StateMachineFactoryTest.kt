package com.prashantbarahi.hateoasdemo

import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleEvent.*
import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleState.*
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineFactory
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineConfigurer
import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleEvent
import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
        factory = StateMachineFactory(configuration)
    }

    @Test
    fun shouldTransitionAsPerConfiguration() {
        val sm = factory.create()

        assertTrue(sm.currentState == DRAFT)
        assertTrue(sm.getNextTransitions().containsAll(listOf(AUTHOR_SUBMIT)))
        sm.sendEvent(AUTHOR_SUBMIT)

        assertTrue(sm.currentState == AUTHOR_SUBMITTED)
        assertTrue(sm.getNextTransitions().containsAll(listOf(TE_APPROVE, TE_REJECT)))

        sm.sendEvent(TE_APPROVE)
        assertTrue(sm.currentState == TE_APPROVED)
        assertTrue(sm.getNextTransitions().containsAll(listOf(FPE_APPROVE, FPE_REJECT)))

        sm.sendEvent(FPE_APPROVE)
        assertTrue(sm.currentState == PUBLISHED)
        assertTrue(sm.getNextTransitions().isEmpty())
    }

    @Test
    fun whenLoadedState_ShouldTransitionAsPerConfiguration() {
        val listOfEvents = listOf(AUTHOR_SUBMIT, TE_REJECT, AUTHOR_SUBMIT, TE_REJECT, AUTHOR_SUBMIT, TE_APPROVE)
        val sm = factory.buildFromHistory(listOfEvents)

        assertEquals(TE_APPROVED, sm.currentState)
        // wrong event
        sm.sendEvent(AUTHOR_SUBMIT)
        assertEquals(TE_APPROVED, sm.currentState)

        sm.sendEvent(FPE_APPROVE)
        assertEquals(PUBLISHED, sm.currentState)
    }

    @Test
    fun testForConcurrency() {
        val executor = Executors.newFixedThreadPool(10)
        val sm = factory.create()
        for (i in 0..10000) {
            val runnable = {
                println(i)

                assertTrue(sm.currentState == DRAFT)
                assertTrue(sm.getNextTransitions().containsAll(listOf(AUTHOR_SUBMIT)))
                sm.sendEvent(AUTHOR_SUBMIT)
                assertTrue(sm.currentState == AUTHOR_SUBMITTED)

                sm.sendEvent(TE_APPROVE)
                assertTrue(sm.getNextTransitions().containsAll(listOf(FPE_APPROVE, FPE_REJECT)))
                sm.sendEvent(FPE_REJECT)
                assertTrue(sm.getNextTransitions().contains(AUTHOR_SUBMIT))

                sm.sendEvent(AUTHOR_SUBMIT)
                assertEquals(TE_APPROVED, sm.currentState)

                assertTrue(sm.currentState == AUTHOR_SUBMITTED)
                assertTrue(sm.getNextTransitions().containsAll(listOf(TE_APPROVE, TE_REJECT)))
                sm.sendEvent(TE_REJECT)
                sm.sendEvent(TE_REJECT)
                assertTrue(sm.currentState == DRAFT)
                sm.sendEvent(AUTHOR_SUBMIT)
                assertTrue(sm.currentState == AUTHOR_SUBMITTED)

            }
            executor.submit(runnable)
        }
        executor.awaitTermination(6, TimeUnit.SECONDS)
    }
}