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

import com.yourcompany.articlereviewworkflow.entities.ArticleEntity
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineFactory
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineFactory.*
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineFactoryProvider
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleState
import com.yourcompany.articlereviewworkflow.statemachine.articles.ReviewType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val repository: ArticleRepository,
    private val stateMachineFactoryProvider: StateMachineFactoryProvider
) {

  fun save(title: String, body: String): ArticleEntity {
    val stateMachineFactory = stateMachineFactoryProvider.getDefaultStateMachine()
    return ArticleEntity
        .create(title = title, body = body, reviewType = stateMachineFactory.identifier as ReviewType)
        .let(repository::save)
  }

  fun findById(id: Long): ArticleEntity = repository.findById(id).orElseThrow()

  fun findAll(): List<ArticleEntity> = repository.findAll()

  @Transactional
  fun handleEvent(articleId: Long, event: ArticleEvent) {
    val article = repository.findById(articleId).orElseThrow()
    val stateMachine = stateMachineFactoryProvider
        .getStateMachineFactory<ArticleState, ArticleEvent>(article.reviewType)
        .buildFromHistory(article.getPastEvents())

    stateMachine.setOnTransitionListener(object : OnStateTransitionListener<ArticleState, ArticleEvent> {
      override fun onTransition(prevState: ArticleState, event: ArticleEvent, nextState: ArticleState) {
        article.state = nextState
        article.consumeEvent(event)
      }
    })
    val eventResult = stateMachine.sendEvent(event)
    if (!eventResult) {
      throw DomainException("Event $event could not be accepted.")
    }
    repository.save(article)
  }

  fun update(articleId: Long, title: String, body: String): ArticleEntity {
    val article = repository.findById(articleId).orElseThrow()
    if (article.state == ArticleState.PUBLISHED) throw DomainException("Published article can not be edited.")
    article.body = body
    article.title = title
    return repository.save(article)
  }

}