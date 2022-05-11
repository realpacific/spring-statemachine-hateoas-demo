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
import com.yourcompany.articlereviewworkflow.models.TaskResource
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineFactoryProvider
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleState
import org.springframework.hateoas.*
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component


@Component
class ArticleTaskAssembler
constructor(
  private val stateMachineFactoryProvider: StateMachineFactoryProvider
) : RepresentationModelAssembler<ArticleEntity, TaskResource> {

  override fun toModel(entity: ArticleEntity): TaskResource {
    val resource = TaskResource(entity.state.name)

    val selfLink = linkTo(methodOn(ArticleController::class.java).getTasks(entity.id!!))
      .withSelfRel()
      .withType("tasks")
      .withTitle("Tasks")

    resource.add(buildApprovalLinkFn(selfLink, getCurrentTasks(entity), entity.id!!))

    return resource
  }


  fun getCurrentTasks(entity: ArticleEntity): List<ArticleEvent> {
    if (entity.isPublished()) return emptyList()

    val stateMachine = stateMachineFactoryProvider
      .getStateMachineFactory<ArticleState, ArticleEvent>(entity.reviewType)
      .buildFromHistory(entity.getPastEvents())

    val nextEvents = stateMachine.getNextTransitions()
    return nextEvents.toList()
  }

  private fun buildApprovalLinkFn(self: Link, events: List<ArticleEvent>, id: Long): Link {
    val configurableAffordance = Affordances.of(self).afford(HttpMethod.PUT) // this is default

    return events.fold(configurableAffordance) { acc, articleEvent ->
      acc.andAfford(HttpMethod.PUT)
        .withName(articleEvent.name)
        .withTarget(
          linkTo(
            methodOn(ArticleController::class.java).handleTask(id, articleEvent.name)
          )
            .withRel("taskActions")
            .withTitle("taskActions")
            .withType("actions")
        )
    }.toLink()
  }
}