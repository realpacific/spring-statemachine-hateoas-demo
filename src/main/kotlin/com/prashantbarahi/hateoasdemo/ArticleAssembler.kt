package com.prashantbarahi.hateoasdemo

import com.prashantbarahi.hateoasdemo.models.ArticleResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.service.StateMachineService
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RequestMethod.PUT

@Component
class ArticleAssembler : RepresentationModelAssembler<ArticleEntity, ArticleResource> {

    @Autowired
    private lateinit var stateMachineService: StateMachineService<ArticleState, ArticleEvent>

    override fun toModel(entity: ArticleEntity): ArticleResource {
        val resource = ArticleResource(
            body = entity.body,
            title = entity.title,
            id = entity.id!!,
            state = entity.state,
            updatedDate = entity.updatedDate,
            createdDate = entity.createdDate
        )
        resource.add(
            linkTo<ArticleController> {
                this.getById(entity.id!!)
            }.withSelfRel().withType(GET.name)
        )
        resource.addIf(entity.state != ArticleState.PUBLISHED) {
            linkTo<ArticleController> {
                this.updateArticle(entity.id!!, null)
            }.withRel("update").withType(PUT.name)
        }
        resource.add(
            linkTo<ArticleController> {
                this.getTasks(entity.id!!)
            }.withRel("tasks").withType(GET.name)
        )
        return resource
    }

    fun buildTasks(entity: ArticleEntity): EntityModel<Links> {
        if (entity.state == ArticleState.PUBLISHED) {
            return EntityModel.of(Links.NONE)
        }

        val stateMachine = stateMachineService.acquireStateMachine(entity.id.toString())

        val nextEvents = determineNextPossibleEvents(stateMachine)
        val approvalLinkBuilderFn = buildApprovalLinkFn(entity.id!!)
        val links = Links.of(nextEvents.map(approvalLinkBuilderFn))

        return EntityModel.of(links)
    }

    private fun buildApprovalLinkFn(id: Long): (ArticleEvent) -> Link {
        return { event ->
            linkTo<ArticleController> { approve(id, event.name) }
                .withRel(event.name)
                .withTitle(event.name)
                .withType(PUT.name)
        }
    }

    private fun determineNextPossibleEvents(sm: StateMachine<ArticleState, ArticleEvent>) =
        sm.transitions
            .filter { it.source.id == sm.state.id }
            .map { it.trigger.event }

}