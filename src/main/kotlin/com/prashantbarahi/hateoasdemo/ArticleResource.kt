package com.prashantbarahi.hateoasdemo

import org.hibernate.annotations.GenericGenerator
import org.springframework.hateoas.EntityModel
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

class ArticleResource : EntityModel<ArticleResource>() {

    var id: Long? = null

    var state: ArticleState? = null

    var title: String = ""

    @Column(length = 5000)
    var body: String = ""
}