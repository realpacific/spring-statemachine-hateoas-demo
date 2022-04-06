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

}