package com.prashantbarahi.hateoasdemo

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.AbstractPersistable
import org.springframework.statemachine.config.EnableStateMachine
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "tbl_article")
open class ArticleEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @field:Column
    @field:Enumerated(EnumType.STRING)
    var state: ArticleState = ArticleState.DRAFT

    @field:Column
    var title: String = ""

    @field:Column(length = 5000)
    var body: String = ""

    @field:UpdateTimestamp
    lateinit var updatedDate: LocalDateTime

    @field:CreationTimestamp
    lateinit var createdDate: LocalDateTime

    companion object {
        fun create(title: String, body: String): ArticleEntity {
            return ArticleEntity().apply {
                this.title = title
                this.body = body
            }
        }
    }
}