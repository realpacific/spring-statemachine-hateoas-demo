package com.prashantbarahi.hateoasdemo

import com.prashantbarahi.hateoasdemo.models.ArticleRequest
import com.prashantbarahi.hateoasdemo.models.ArticleResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.Link
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/articles")
@CrossOrigin(origins = ["*"])
class ArticleController {

    @Autowired
    private lateinit var assembler: ArticleAssembler

    @Autowired
    private lateinit var service: ArticleService

    @GetMapping()
    fun getAll(): List<ArticleResource> {
        return service.findAll().map(assembler::toModel).toList()
    }

    @GetMapping("/{articleId}")
    fun getById(@PathVariable articleId: Long): ArticleResource {
        return service.findById(articleId).let(assembler::toModel)
    }

    @PostMapping
    fun createArticle(@RequestBody request: ArticleRequest): ArticleResource {
        return service.save(request.title, request.body).let(assembler::toModel)
    }

    @PutMapping("/{articleId}")
    fun updateArticle(@PathVariable articleId: Long, @RequestBody body: ArticleRequest?): ArticleResource {
        require(body != null)
        return service.update(articleId, body.title, body.body).let(assembler::toModel)
    }

    @GetMapping("/{articleId}/tasks")
    fun getTasks(@PathVariable articleId: Long): List<Link> {
        val article = service.findById(articleId)
        return assembler.buildTasks(article).content!!.toList()
    }

    @PutMapping("/{articleId}/tasks/{task}")
    fun approve(@PathVariable articleId: Long, @PathVariable task: String): List<Link> {
        service.handleEvent(articleId, ArticleEvent.valueOf(task.uppercase()))
        return service.findById(articleId).let(assembler::toModel).links.toList()
    }

}