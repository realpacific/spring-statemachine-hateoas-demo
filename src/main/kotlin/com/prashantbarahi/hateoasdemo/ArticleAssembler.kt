package com.prashantbarahi.hateoasdemo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.statemachine.service.StateMachineService
import org.springframework.stereotype.Component

@Component
class ArticleAssembler : RepresentationModelAssembler<Article, ArticleResource> {

    @Autowired
    private lateinit var stateMachineService: StateMachineService<ArticleState, ArticleEvent>

    override fun toModel(entity: Article): ArticleResource {
        val resource = ArticleResource().apply {
            body = entity.body
            title = entity.title
            id = entity.id
            state = entity.state
        }
        resource.add(
            linkTo<ArticleController> {
                this.getById(entity.id!!)
            }.withSelfRel()
                .withType("GET")
        )

        val sm = stateMachineService.acquireStateMachine(entity.id.toString())
        val nextEvents = sm.transitions.filter { it.source.id == sm.state.id }.map { it.trigger.event }

        nextEvents.map {
            resource.add(
                linkTo<ArticleController> { approve(entity.id!!, it.name) }
                    .withRel(it.name)
                    .withType("PUT")
            )
        }

        return resource
    }


}