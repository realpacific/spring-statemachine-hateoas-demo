package com.prashantbarahi.hateoasdemo

import com.prashantbarahi.hateoasdemo.ArticleEvent.*
import com.prashantbarahi.hateoasdemo.ArticleState.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

internal class StateMachineFactoryTest {

    private lateinit var factory: StateMachineFactory<ArticleState, ArticleEvent>

    @BeforeEach
    fun setup() {
        val config = StateMachineConfig.Builder<ArticleState, ArticleEvent>()
            .withStartState(DRAFT)
            .withEndState(PUBLISHED)
            .withStates(EnumSet.allOf(ArticleState::class.java))
            .build()

        factory = StateMachineFactory(config)
        factory.addTransition(start = DRAFT, end = AUTHOR_SUBMITTED, trigger = AUTHOR_SUBMIT)
        factory.addTransition(start = AUTHOR_SUBMITTED, end = TE_APPROVED, trigger = TE_APPROVE)
        factory.addTransition(start = AUTHOR_SUBMITTED, end = DRAFT, trigger = TE_REJECT)
        factory.addTransition(start = TE_APPROVED, end = PUBLISHED, trigger = FPE_APPROVE)
        factory.addTransition(start = TE_APPROVED, end = DRAFT, trigger = FPE_REJECT)
    }

    @Test
    fun shouldTransitionAsPerConfiguration() {
        val sm = factory.create(id = 2)

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

        val sm2 = factory.createFromState(id = 1, state = TE_APPROVED)
        assertTrue(sm2.getNextTransitions().containsAll(listOf(FPE_APPROVE, FPE_REJECT)))
        sm2.sendEvent(FPE_REJECT)
        assertTrue(sm2.currentState == DRAFT)
        assertTrue(sm2.getNextTransitions().contains(AUTHOR_SUBMIT))
    }

    @Test
    fun whenLoadedState_ShouldTransitionAsPerConfiguration() {
        val sm = factory.createFromState(id = 1, state = TE_APPROVED)
        factory.setOnTransitionListener { prev, event, next ->
            println("$prev --($event)--> $next")
        }
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
        val sm = factory.create(id = 2)
        for (i in 0..10000) {
            val runnable = {
                println(i)

                assertTrue(sm.currentState == DRAFT)
                assertTrue(sm.getNextTransitions().containsAll(listOf(AUTHOR_SUBMIT)))
                sm.sendEvent(AUTHOR_SUBMIT)
                assertTrue(sm.currentState == AUTHOR_SUBMITTED)

                if (ThreadLocalRandom.current().nextBoolean()) {
                    sm.sendEvent(TE_APPROVE)
                    assertTrue(sm.getNextTransitions().containsAll(listOf(FPE_APPROVE, FPE_REJECT)))
                } else {
                    sm.sendEvent(TE_REJECT)
                    assertTrue(sm.currentState == DRAFT)
                    sm.sendEvent(AUTHOR_SUBMIT)
                    assertTrue(sm.currentState == AUTHOR_SUBMITTED)
                    sm.sendEvent(TE_APPROVE)
                }
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