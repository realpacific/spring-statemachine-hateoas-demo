package com.yourcompany.articlereviewworkflow.statemachine.articles

import org.springframework.stereotype.Service


@Service
class ArticleEventMapper {

  init {
    validateArticleEventAliasNotBlank()
    validateArticleEventAliasUniqueness()
  }

  private fun validateArticleEventAliasNotBlank() {
    require(ArticleEvent.values().map(ArticleEvent::alias).all { it.isNotBlank() }) {
      "ArticleEvent alias must not be blank"
    }
  }

  private fun validateArticleEventAliasUniqueness() {
    val events = ArticleEvent.values()
    require(events.size == events.map(ArticleEvent::alias).distinct().size) {
      "ArticleEvent alias must be unique"
    }
  }

  fun getArticleEvent(alias: String): ArticleEvent? {
    return ArticleEvent.values().find { it.alias == alias }
  }

}