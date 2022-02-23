package com.prashantbarahi.hateoasdemo

import org.springframework.hateoas.EntityModel
import java.time.LocalDateTime

open class ArticleResource
constructor(
    var id: Long,
    var state: ArticleState,
    var title: String,
    var body: String,
    var updatedDate: LocalDateTime,
    var createdDate: LocalDateTime,
) : EntityModel<ArticleResource>()