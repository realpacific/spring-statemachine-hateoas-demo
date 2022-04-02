package com.prashantbarahi.hateoasdemo.models

import com.prashantbarahi.hateoasdemo.statemachine.articles.ArticleState
import org.springframework.hateoas.EntityModel
import java.time.LocalDateTime

open class ArticleResource
constructor(
    val id: Long,
    val state: ArticleState,
    val title: String,
    val body: String,
    val updatedDate: LocalDateTime,
    val createdDate: LocalDateTime,
    val reviewType: String
) : EntityModel<ArticleResource>()