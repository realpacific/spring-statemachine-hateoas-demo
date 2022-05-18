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
import com.yourcompany.articlereviewworkflow.models.ArticleResource
import com.yourcompany.articlereviewworkflow.models.ArticleRequest
import com.yourcompany.articlereviewworkflow.statemachine.StateMachineFactoryProvider
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleEvent
import com.yourcompany.articlereviewworkflow.statemachine.articles.ArticleState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.*
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@Component
class ArticleAssembler @Autowired constructor(
  private val stateMachineFactoryProvider: StateMachineFactoryProvider
) : RepresentationModelAssembler<ArticleEntity, ArticleResource> {

  companion object Rel {
    private const val ACTIONS = "actions"
    private const val UPDATE = "update"
  }

  override fun toModel(entity: ArticleEntity): ArticleResource {
    val resource = ArticleResource(
      body = entity.body,
      title = entity.title,
      id = entity.id!!,
      state = entity.state,
      updatedDate = entity.updatedDate,
      createdDate = entity.createdDate,
      reviewType = entity.reviewType.name
    )

    val resourceLink = buildSelfLink(entity)
      .addDefaultAffordance() // a hack to offset other affordances, 1st affordance is always named "default"
      .addUpdateAffordance(entity)
      .addActionsAffordances(entity)

    resource.add(resourceLink)

    return resource
  }

  fun buildSelfLink(entity: ArticleEntity): Link {
    return linkTo(methodOn(ArticleController::class.java).getById(entity.id!!)).withSelfRel()
  }

  private fun Link.addDefaultAffordance(): Link {
    val configurableAffordance = Affordances.of(this)
    return configurableAffordance.afford(HttpMethod.TRACE).toLink()
  }

  private fun Link.addUpdateAffordance(entity: ArticleEntity): Link {
    if (entity.isPublished()) return this
    val configurableAffordance = Affordances.of(this)
    return configurableAffordance.afford(HttpMethod.PUT)
      .withName(UPDATE)
      .withTarget(
        linkTo(
          methodOn(ArticleController::class.java)
            .updateArticle(entity.id!!, null)
        ).withRel(UPDATE)
      )
      .withInput(ArticleRequest::class.java)
      .toLink()
  }

  private fun getAvailableActions(entity: ArticleEntity): List<ArticleEvent> {
    if (entity.isPublished()) return emptyList()

    val stateMachine = stateMachineFactoryProvider
      .getStateMachineFactory<ArticleState, ArticleEvent>(entity.reviewType)
      .buildFromHistory(entity.getPastEvents())

    val nextEvents = stateMachine.getNextTransitions()
    return nextEvents.toList()
  }


  private fun Link.addActionsAffordances(entity: ArticleEntity): Link {
    val buildActionTargetFn: (ArticleEvent) -> Link = { event ->
      linkTo(
        methodOn(ArticleController::class.java)
          .handleAction(entity.id!!, event.alias)
      ).withRel(ACTIONS)
    }
    val events = getAvailableActions(entity)
    if (events.isEmpty()) return this
    val configurableAffordance = Affordances.of(this).afford(HttpMethod.POST)
      .withName(events.first().name)
      .withTarget(buildActionTargetFn(events.first()))

    return events.subList(1, events.size)
      .fold(configurableAffordance) { acc, articleEvent ->
        acc.andAfford(HttpMethod.POST)
          .withName(articleEvent.name)
          .withTarget(buildActionTargetFn(articleEvent))
      }.toLink()
  }
}