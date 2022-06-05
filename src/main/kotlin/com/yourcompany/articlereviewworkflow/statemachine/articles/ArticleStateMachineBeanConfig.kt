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

package com.yourcompany.articlereviewworkflow.statemachine.articles

import com.yourcompany.articlereviewworkflow.statemachine.StateMachineConfigurer
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineFactory
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent.*
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleState.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

const val THREE_LEVEL_REVIEW_STATE_MACHINE = "ThreeLevelReviewStateMachineFactory"
const val FOUR_LEVEL_REVIEW_STATE_MACHINE = "FourLevelReviewStateMachineFactory"
const val FIVE_LEVEL_REVIEW_STATE_MACHINE = "FiveLevelReviewStateMachineFactory"

@Configuration
class ArticleStateMachineBeanConfig {

  @Bean(THREE_LEVEL_REVIEW_STATE_MACHINE)
  fun providesThreeLevelReviewStateMachineFactory(): ArticleStateMachineFactory {
    val configuration = StateMachineConfigurer.StateBuilder<ArticleState, ArticleEvent>()
      .withStartState(DRAFT)
      .withEndState(PUBLISHED)
      .withStates(DRAFT, AUTHOR_SUBMITTED, TE_APPROVED, PUBLISHED)
      .and()
      .withTransitions {

        // Author
        defineTransition(start = DRAFT, trigger = AUTHOR_SUBMIT, end = AUTHOR_SUBMITTED)

        // TE
        defineTransition(start = AUTHOR_SUBMITTED, trigger = TE_APPROVE, end = TE_APPROVED)
        defineTransition(start = AUTHOR_SUBMITTED, trigger = TE_REJECT, end = DRAFT)

        // FPE
        defineTransition(start = TE_APPROVED, trigger = FPE_APPROVE, end = PUBLISHED)
        defineTransition(start = TE_APPROVED, trigger = FPE_REJECT, end = DRAFT)
      }
    return StateMachineFactory(ReviewType.THREE_LEVEL_WORKFLOW, configuration)
  }

  @Bean(FOUR_LEVEL_REVIEW_STATE_MACHINE)
  fun providesFourLevelReviewStateMachineFactory(): ArticleStateMachineFactory {
    val configuration = StateMachineConfigurer.StateBuilder<ArticleState, ArticleEvent>()
      .withStartState(DRAFT)
      .withEndState(PUBLISHED)
      .withStates(DRAFT, AUTHOR_SUBMITTED, TE_APPROVED, EDITOR_DONE, PUBLISHED)
      .and()
      .withTransitions {

        // Author
        defineTransition(start = DRAFT, trigger = AUTHOR_SUBMIT, end = AUTHOR_SUBMITTED)

        // TE
        defineTransition(start = AUTHOR_SUBMITTED, trigger = TE_APPROVE, end = TE_APPROVED)
        defineTransition(start = AUTHOR_SUBMITTED, trigger = TE_REJECT, end = DRAFT)

        // Editor
        defineTransition(start = TE_APPROVED, trigger = EDITOR_APPROVE, end = EDITOR_DONE)

        // FPE
        defineTransition(start = EDITOR_DONE, trigger = FPE_APPROVE, end = PUBLISHED)
        defineTransition(start = EDITOR_DONE, trigger = FPE_REJECT, end = DRAFT)
      }
    return StateMachineFactory(ReviewType.FOUR_LEVEL_WORKFLOW, configuration)
  }

  @Primary
  @Bean(FIVE_LEVEL_REVIEW_STATE_MACHINE)
  fun providesFiveLevelReviewStateMachineFactory(): ArticleStateMachineFactory {
    val configuration = StateMachineConfigurer.StateBuilder<ArticleState, ArticleEvent>()
      .withStartState(DRAFT)
      .withEndState(PUBLISHED)
      .withStates(DRAFT, AUTHOR_SUBMITTED, TE_APPROVED, EDITOR_DONE, ILLUSTRATOR_DONE, PUBLISHED)
      .and()
      .withTransitions {

        // Author
        defineTransition(start = DRAFT, trigger = AUTHOR_SUBMIT, end = AUTHOR_SUBMITTED)

        // TE
        defineTransition(start = AUTHOR_SUBMITTED, trigger = TE_APPROVE, end = TE_APPROVED)
        defineTransition(start = AUTHOR_SUBMITTED, trigger = TE_REJECT, end = DRAFT)

        // Editor
        defineTransition(start = TE_APPROVED, trigger = EDITOR_APPROVE, end = EDITOR_DONE)

        // Illustrator
        defineTransition(start = EDITOR_DONE, trigger = ILLUSTRATOR_APPROVE, end = ILLUSTRATOR_DONE)

        // FPE
        defineTransition(start = ILLUSTRATOR_DONE, trigger = FPE_APPROVE, end = PUBLISHED)
        defineTransition(start = ILLUSTRATOR_DONE, trigger = FPE_REJECT, end = DRAFT)
      }
    return StateMachineFactory(ReviewType.FIVE_LEVEL_WORKFLOW, configuration)
  }
}