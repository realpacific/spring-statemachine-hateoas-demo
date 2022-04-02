package com.prashantbarahi.hateoasdemo.entities

import com.prashantbarahi.hateoasdemo.ArticleEvent
import com.prashantbarahi.hateoasdemo.ArticleState
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "tbl_article")
class ArticleEntity {

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @field:Column
    @field:Enumerated(EnumType.STRING)
    var state: ArticleState = ArticleState.DRAFT

    @field:Column(nullable = false)
    var title: String = ""

    @field:Column(length = 5000)
    var body: String = ""

    @field:UpdateTimestamp
    lateinit var updatedDate: LocalDateTime
        private set

    @field:CreationTimestamp
    lateinit var createdDate: LocalDateTime
        private set

    @field:OneToOne(mappedBy = "article", orphanRemoval = true, cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    private lateinit var history: EventHistoryEntity

    @field:Column(nullable = false)
    @field:Enumerated(EnumType.STRING)
    lateinit var reviewType: ReviewType
        private set

    fun getPastEvents(): List<ArticleEvent> {
        return history.events
    }

    fun consumeEvent(event: ArticleEvent) {
        history.events.add(event)
    }

    companion object {
        fun create(title: String, body: String, reviewType: ReviewType): ArticleEntity {
            require(title.isNotBlank())
            return ArticleEntity().apply {
                this.title = title
                this.body = body
                this.reviewType = reviewType
                this.history = EventHistoryEntity.new(this)
            }
        }
    }
}