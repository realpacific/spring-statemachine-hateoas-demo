package com.prashantbarahi.hateoasdemo

import com.prashantbarahi.hateoasdemo.entities.ArticleEntity
import com.prashantbarahi.hateoasdemo.statemachine.StateMachineFactory.OnStateTransitionListener
import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleStateMachineFactoryOfFactories
import com.prashantbarahi.hateoasdemo.statemachine.articles.FOUR_LEVEL_REVIEW_STATE_MACHINE
import com.prashantbarahi.hateoasdemo.statemachine.articles.THREE_LEVEL_REVIEW_STATE_MACHINE
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArticleService(
    private val repository: ArticleRepository,
    private val stateMachineService: ArticleStateMachineFactoryOfFactories
) {

    fun save(title: String, body: String): ArticleEntity = ArticleEntity.create(title, body).let(repository::save)

    fun findById(id: Long): ArticleEntity = repository.findById(id).orElseThrow()

    fun findAll(): List<ArticleEntity> = repository.findAll()

    @Transactional
    fun handleEvent(articleId: Long, event: ArticleEvent) {
        val article = repository.findById(articleId).orElseThrow()
        val stateMachine = stateMachineService
            .getStateMachineFactory(FOUR_LEVEL_REVIEW_STATE_MACHINE)
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