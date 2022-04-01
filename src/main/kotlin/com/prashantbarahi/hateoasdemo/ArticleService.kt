package com.prashantbarahi.hateoasdemo

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val repository: ArticleRepository,
    private val stateMachineService: StateMachineFactory<ArticleState, ArticleEvent>
) {

    fun save(title: String, body: String): ArticleEntity = ArticleEntity.create(title, body).let(repository::save)

    fun findById(id: Long): ArticleEntity = repository.findById(id).orElseThrow()

    fun findAll(): List<ArticleEntity> = repository.findAll()

    @Transactional
    fun handleEvent(articleId: Long, event: ArticleEvent) {
        val article = repository.findById(articleId).orElseThrow()
        val stateMachine = stateMachineService.createFromState(articleId, article.state)
        val eventResult = stateMachine.sendEvent(event)

        if (!eventResult) {
            throw DomainException("Event $event could not be accepted.")
        }
        article.state = stateMachine.currentState
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