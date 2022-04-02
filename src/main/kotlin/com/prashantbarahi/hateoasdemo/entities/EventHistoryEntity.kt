package com.prashantbarahi.hateoasdemo.entities

import com.prashantbarahi.hateoasdemo.ArticleEvent
import javax.persistence.*

@Entity(name = "tbl_history")
class EventHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @field:OneToOne
    var article: ArticleEntity? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(value = EnumType.STRING)
    val events = mutableListOf<ArticleEvent>()

    companion object {
        fun new(article: ArticleEntity): EventHistoryEntity {
            return EventHistoryEntity().apply {
                this.article = article
            }
        }
    }
}