package com.prashantbarahi.hateoasdemo

import org.springframework.hateoas.EntityModel
import javax.persistence.Column

open class ArticleResource : EntityModel<ArticleResource>() {

    var id: Long? = null

    var state: ArticleState? = null

    var title: String = ""

    @Column(length = 5000)
    var body: String = ""
}