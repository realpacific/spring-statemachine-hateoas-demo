package com.yourcompany.articlereviewworkflow.statemachine.articles

import org.springframework.stereotype.Service


@Service
class ArticleEventMapper {

  init {
    validateArticleEventAliasUniqueness()
  }

  private fun validateArticleEventAliasUniqueness() {
    require(ArticleEvent.values().size == ArticleEvent.values().map { it.alias }.distinct().size) {
      "ArticleEvent alias must be unique"
    }
  }

  fun getArticleEvent(alias: String): ArticleEvent? {
    return ArticleEvent.values().find { it.alias == alias }
  }

}