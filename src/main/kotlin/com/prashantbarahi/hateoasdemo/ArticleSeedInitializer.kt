package com.prashantbarahi.hateoasdemo

import com.fasterxml.jackson.databind.ObjectMapper
import com.prashantbarahi.hateoasdemo.models.ArticleRequest
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ArticleSeedInitializer
constructor(
    private val objectMapper: ObjectMapper,
    private val service: ArticleService
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        val stream = ArticleController::class.java.classLoader.getResourceAsStream("articles.json")!!
        objectMapper.readerForListOf(ArticleRequest::class.java)
            .readValue<List<ArticleRequest>>(stream)
            .forEach {
                service.save(it.title, it.body)
            }
    }
}