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

    fun getPastEvents(): List<ArticleEvent> {
        return history.events
    }

    fun consumeEvent(event: ArticleEvent) {
        history.events.add(event)
    }

    fun getStateMachineName() = history.handledBy!!

    companion object {
        fun create(title: String, body: String, handler: String): ArticleEntity {
            require(title.isNotBlank())
            return ArticleEntity().apply {
                this.title = title
                this.body = body
                this.history = EventHistoryEntity.new(handler, this)
            }
        }
    }
}