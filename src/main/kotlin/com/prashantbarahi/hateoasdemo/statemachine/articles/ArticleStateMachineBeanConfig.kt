package com.prashantbarahi.hateoasdemo.statemachine.articles

import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleEvent.*
import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleState.*
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineFactory
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

const val THREE_LEVEL_REVIEW_STATE_MACHINE = "ThreeLevelReviewStateMachineFactory"
const val FOUR_LEVEL_REVIEW_STATE_MACHINE = "FourLevelReviewStateMachineFactory"

@Configuration
class ArticleStateMachineBeanConfig {

  @Primary
  @Bean(THREE_LEVEL_REVIEW_STATE_MACHINE)
  fun providesThreeLevelReviewStateMachineFactory(): ArticleStateMachineFactory {
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
    return StateMachineFactory(ReviewType.THREE_LEVEL_WORKFLOW, configuration)
  }

  @Bean(FOUR_LEVEL_REVIEW_STATE_MACHINE)
  fun providesFourLevelReviewStateMachineFactory(): ArticleStateMachineFactory {
    val configuration = StateMachineConfigurer.StateBuilder<ArticleState, ArticleEvent>()
        .withStartState(DRAFT)
        .withEndState(PUBLISHED)
        .withStates(DRAFT, AUTHOR_SUBMITTED, TE_APPROVED, EDITOR_APPROVED, PUBLISHED)
        .and()
        .withTransitions {

          // Author
          defineTransition(start = DRAFT, end = AUTHOR_SUBMITTED, trigger = AUTHOR_SUBMIT)

          // TE
          defineTransition(start = AUTHOR_SUBMITTED, end = TE_APPROVED, trigger = TE_APPROVE)
          defineTransition(start = AUTHOR_SUBMITTED, end = DRAFT, trigger = TE_REJECT)

          // Editor
          defineTransition(start = TE_APPROVED, end = EDITOR_APPROVED, trigger = EDITOR_APPROVE)

          // FPE
          defineTransition(start = EDITOR_APPROVED, end = PUBLISHED, trigger = FPE_APPROVE)
          defineTransition(start = EDITOR_APPROVED, end = DRAFT, trigger = FPE_REJECT)
        }
    return StateMachineFactory(ReviewType.FOUR_LEVEL_WORKFLOW, configuration)
  }
}