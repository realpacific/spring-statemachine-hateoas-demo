package com.prashantbarahi.hateoasdemo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
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
            }.withSelfRel().withType("GET"),
        )
        resource.add(
            linkTo<ArticleController> {
                this.getTasks(entity.id!!)
            }.withRel("tasks").withType("GET")
        )
        return resource
    }

    fun buildTasks(entity: Article): EntityModel<Links> {

        if (entity.state == ArticleState.PUBLISHED) return EntityModel.of(Links.NONE)
        val sm = stateMachineService.acquireStateMachine(entity.id.toString())
        val nextEvents = sm.transitions.filter { it.source.id == sm.state.id }.map { it.trigger.event }
        val links = Links.of(nextEvents.map {
            linkTo<ArticleController> { approve(entity.id!!, it.name) }
                .withRel(it.name)
                .withType("PUT")
        })
        return EntityModel.of(links)

    }


}