package com.prashantbarahi.hateoasdemo

import org.springframework.data.jpa.domain.AbstractPersistable
import org.springframework.statemachine.config.EnableStateMachine
import javax.persistence.*

@Entity(name = "tbl_article")
@EnableStateMachine
open class Article : AbstractPersistable<Long>() {

    @field:Column
    var state: ArticleState = ArticleState.DRAFT

    @field:Column
    var title: String = ""

    @field:Column(length = 5000)
    var body: String = ""


    companion object {
        fun create(title: String, body: String): Article {
            return Article().apply {
                this.title = title
                this.body = body
            }
        }
    }
}