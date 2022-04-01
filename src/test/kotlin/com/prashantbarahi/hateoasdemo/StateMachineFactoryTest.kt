package com.prashantbarahi.hateoasdemo

import com.prashantbarahi.hateoasdemo.ArticleEvent.*
import com.prashantbarahi.hateoasdemo.ArticleState.*
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineFactory
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineFactory.OnStateTransitionListener
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineStateConfigurer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class StateMachineFactoryTest {

    private lateinit var factory: StateMachineFactory<ArticleState, ArticleEvent>

    @BeforeEach
    fun setup() {
        val config = StateMachineStateConfigurer.StateBuilder<ArticleState, ArticleEvent>()
            .withStartState(ArticleState.DRAFT)
            .withEndState(ArticleState.PUBLISHED)
            .withStates(EnumSet.allOf(ArticleState::class.java))
            .and()

        val transitionConfig = StateTransitionConfigurer(config)
            .addTransition(
                start = ArticleState.DRAFT,
                end = ArticleState.AUTHOR_SUBMITTED,
                trigger = ArticleEvent.AUTHOR_SUBMIT
            )
            .addTransition(
                start = ArticleState.AUTHOR_SUBMITTED,
                end = ArticleState.TE_APPROVED,
                trigger = ArticleEvent.TE_APPROVE
            )
            .addTransition(
                start = ArticleState.AUTHOR_SUBMITTED,
                end = ArticleState.DRAFT,
                trigger = ArticleEvent.TE_REJECT
            )
            .addTransition(
                start = ArticleState.TE_APPROVED,
                end = ArticleState.PUBLISHED,
                trigger = ArticleEvent.FPE_APPROVE
            )
            .addTransition(
                start = ArticleState.TE_APPROVED,
                end = ArticleState.DRAFT,
                trigger = ArticleEvent.FPE_REJECT
            )

        factory = StateMachineFactory(transitionConfig)
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
        factory.setOnTransitionListener(object : OnStateTransitionListener<ArticleState, ArticleEvent> {
            override fun onTransition(prevState: ArticleState, event: ArticleEvent, nextState: ArticleState) {
                println("$prevState --($event)--> $nextState")
            }
        })
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