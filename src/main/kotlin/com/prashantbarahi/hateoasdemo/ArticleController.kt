package com.prashantbarahi.hateoasdemo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.statemachine.StateMachinePersist
import org.springframework.statemachine.service.StateMachineService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/articles")
class ArticleController : CommandLineRunner {

    @Autowired
    private lateinit var stateMachineService: StateMachineService<ArticleState, ArticleEvent>

    @Autowired
    private lateinit var persist: StateMachinePersist<ArticleState, ArticleEvent, String>

    @Autowired
    private lateinit var assembler: ArticleAssembler

    @Autowired
    private lateinit var repository: ArticleRepository

    @GetMapping()
    fun getAll(): List<ArticleResource> {
        return repository.findAll().map { assembler.toModel(it) }.toList()
    }


    @GetMapping("/{articleId}")
    fun getById(@PathVariable articleId: Long): Article {
        return repository.findById(articleId).orElseThrow()
    }

    @GetMapping("/{articleId}/tasks")
    fun getTasks(@PathVariable articleId: Long): Links {
        val article = repository.findById(articleId).orElseThrow()
        return assembler.toModel(article).links
    }


    @PutMapping("/{articleId}/tasks/{task}")
    fun approve(@PathVariable articleId: Long, @PathVariable task: String): List<Link> {
        val article = repository.findById(articleId).orElseThrow()

        var stateMachine = stateMachineService.acquireStateMachine(articleId.toString(), true)
        stateMachine.sendEvent(ArticleEvent.valueOf(task.uppercase()).withMessage(articleId))
        stateMachine = stateMachineService.acquireStateMachine(articleId.toString(), true)
        return assembler.toModel(repository.findById(articleId).orElseThrow()).links.toList()
    }


    override fun run(vararg args: String?) {
        repository.saveAll(
            listOf(
                Article.create("Getting Started with Cucumber", ""),
                Article.create("Mobile Development 101", "")
            )
        )
    }


}