package com.prashantbarahi.hateoasdemo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.Links
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/articles")
@CrossOrigin(origins = ["http://localhost:3000"])
class ArticleController : CommandLineRunner {
    @Autowired
    private lateinit var assembler: ArticleAssembler

    @Autowired
    private lateinit var repository: ArticleRepository

    @Autowired
    private lateinit var service: ArticleService

    @GetMapping()
    fun getAll(): List<ArticleResource> {
        return repository.findAll().map { assembler.toModel(it) }.toList()
    }

    @GetMapping("/{articleId}")
    fun getById(@PathVariable articleId: Long): ArticleResource {
        return assembler.toModel(repository.findById(articleId).orElseThrow())
    }

    @GetMapping("/{articleId}/tasks")
    fun getTasks(@PathVariable articleId: Long): List<Link> {
        val article = repository.findById(articleId).orElseThrow()
        return assembler.buildTasks(article).content!!.toList()
    }


    @PutMapping("/{articleId}/tasks/{task}")
    fun approve(@PathVariable articleId: Long, @PathVariable task: String): List<Link> {
        service.handleEvent(articleId, ArticleEvent.valueOf(task.uppercase()))
        return assembler.toModel(repository.findById(articleId).orElseThrow()).links.toList()
    }


    override fun run(vararg args: String?) {
        repository.saveAll(
            listOf(
                Article.create(
                    "Getting Started with Cucumber",
                    "Learn to use Cucumber, Gherkin, Hamcrest and Rest Assured to integrate Behavior-Driven Development (BDD) in an application made using Spring Boot and Kotlin."
                ),
                Article.create(
                    "RxJava Combining Operators",
                    "RxJava is a ReactiveX port for Java and a programming paradigm that provides a way to put static items into motion. It comes packed with many intuitive APIs to merge, filter, transform or otherwise alter the stream of data."
                )
            )
        )
    }


}