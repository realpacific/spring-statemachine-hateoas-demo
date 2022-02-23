package com.prashantbarahi.hateoasdemo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.statemachine.service.StateMachineService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    @Autowired private val repository: ArticleRepository,
    private val stateMachineService: StateMachineService<ArticleState, ArticleEvent>
) {

    fun save(title: String, body: String): ArticleEntity {
        return ArticleEntity.create(title, body).let(repository::save)
    }

    @Transactional
    fun handleEvent(articleId: Long, event: ArticleEvent) {
        val article = repository.findById(articleId).orElseThrow()
        val stateMachine = stateMachineService.acquireStateMachine(articleId.toString(), true)
        val eventResult = stateMachine.sendEvent(event.withMessage(articleId))

        if (!eventResult) {
            throw RuntimeException("Event $event could not be accepted.")
        }
        article.state = stateMachine.state.id
        repository.save(article)
    }

    fun update(articleId: Long, title: String, body: String): ArticleEntity {
        val article = repository.findById(articleId).orElseThrow()
        if (article.state == ArticleState.PUBLISHED) throw RuntimeException()
        article.body = body
        article.title = title
        return repository.save(article)
    }

}