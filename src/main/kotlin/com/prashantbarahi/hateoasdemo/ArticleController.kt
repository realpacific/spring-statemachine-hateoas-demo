package com.prashantbarahi.hateoasdemo

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.hateoas.Link
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.toPath

@RestController
@RequestMapping("/articles")
@CrossOrigin(origins = ["http://localhost:3000"])
class ArticleController : CommandLineRunner {
    @Autowired
    private lateinit var assembler: ArticleAssembler

    @Autowired
    private lateinit var service: ArticleService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @GetMapping()
    fun getAll(): List<ArticleResource> {
        return service.findAll().map { assembler.toModel(it) }.toList()
    }

    @GetMapping("/{articleId}")
    fun getById(@PathVariable articleId: Long): ArticleResource {
        return assembler.toModel(service.findById(articleId))
    }

    @PostMapping
    fun createArticle(@RequestBody body: ArticleRequest): ArticleResource {
        return assembler.toModel(service.save(body.title, body.title))
    }

    @PutMapping("/{articleId}")
    fun updateArticle(@PathVariable articleId: Long, @RequestBody body: ArticleRequest?): ArticleResource {
        require(body != null)
        return assembler.toModel(service.update(articleId, body.title, body.body))
    }

    @GetMapping("/{articleId}/tasks")
    fun getTasks(@PathVariable articleId: Long): List<Link> {
        val article = service.findById(articleId)
        return assembler.buildTasks(article).content!!.toList()
    }

    @PutMapping("/{articleId}/tasks/{task}")
    fun approve(@PathVariable articleId: Long, @PathVariable task: String): List<Link> {
        service.handleEvent(articleId, ArticleEvent.valueOf(task.uppercase()))
        return assembler.toModel(service.findById(articleId)).links.toList()
    }


    override fun run(vararg args: String?) {
        val stream = ArticleController::class.java.classLoader.getResourceAsStream("articles.json")!!
        objectMapper.readerForListOf(ArticleRequest::class.java)
            .readValue<List<ArticleRequest>>(stream)
            .forEach {
                service.save(it.title, it.body)
            }
    }
}