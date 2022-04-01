package com.prashantbarahi.hateoasdemo.statemachine.articles

import com.prashantbarahi.hateoasdemo.ArticleEvent
import com.prashantbarahi.hateoasdemo.ArticleEvent.*
import com.prashantbarahi.hateoasdemo.ArticleState
import com.prashantbarahi.hateoasdemo.ArticleState.*
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineFactory
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineStateConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val THREE_LEVEL_REVIEW_STATE_MACHINE = "ThreeLevelReviewStateMachineFactory"
const val FOUR_LEVEL_REVIEW_STATE_MACHINE = "FourLevelReviewStateMachineFactory"

@Configuration
class ArticleStateMachineBeanConfig {

    @Bean(THREE_LEVEL_REVIEW_STATE_MACHINE)
    fun providesThreeLevelReviewStateMachineFactory(): ArticleStateMachineFactory {
        val configuration = StateMachineStateConfigurer.StateBuilder<ArticleState, ArticleEvent>()
            .withStartState(DRAFT)
            .withEndState(PUBLISHED)
            .withStates(DRAFT, AUTHOR_SUBMITTED, TE_APPROVED, PUBLISHED)
            .and()
            .withTransitions {
                defineTransition(start = DRAFT, end = AUTHOR_SUBMITTED, trigger = AUTHOR_SUBMIT)
                defineTransition(start = AUTHOR_SUBMITTED, end = TE_APPROVED, trigger = TE_APPROVE)
                defineTransition(start = AUTHOR_SUBMITTED, end = DRAFT, trigger = TE_REJECT)
                defineTransition(start = TE_APPROVED, end = PUBLISHED, trigger = FPE_APPROVE)
                defineTransition(start = TE_APPROVED, end = DRAFT, trigger = FPE_REJECT)
            }
        return StateMachineFactory(configuration)
    }

    @Bean(FOUR_LEVEL_REVIEW_STATE_MACHINE)
    fun providesFourLevelReviewStateMachineFactory(): ArticleStateMachineFactory {
        val configuration = StateMachineStateConfigurer.StateBuilder<ArticleState, ArticleEvent>()
            .withStartState(DRAFT)
            .withEndState(PUBLISHED)
            .withStates(DRAFT, AUTHOR_SUBMITTED, TE_APPROVED, EDITOR_APPROVED, PUBLISHED)
            .and()
            .withTransitions {
                defineTransition(start = DRAFT, end = AUTHOR_SUBMITTED, trigger = AUTHOR_SUBMIT)
                defineTransition(start = AUTHOR_SUBMITTED, end = TE_APPROVED, trigger = TE_APPROVE)
                defineTransition(start = AUTHOR_SUBMITTED, end = DRAFT, trigger = TE_REJECT)
                defineTransition(start = TE_APPROVED, end = EDITOR_APPROVED, trigger = EDITOR_APPROVE)
                defineTransition(start = EDITOR_APPROVED, end = PUBLISHED, trigger = FPE_APPROVE)
                defineTransition(start = EDITOR_APPROVED, end = DRAFT, trigger = FPE_REJECT)
            }
        return StateMachineFactory(configuration)
    }
}