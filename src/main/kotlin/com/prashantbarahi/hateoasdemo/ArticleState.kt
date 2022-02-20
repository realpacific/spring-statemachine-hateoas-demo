package com.prashantbarahi.hateoasdemo

enum class ArticleState {
    DRAFT,
    AUTHOR_SUBMITTED,
    TE_APPROVED,
    PUBLISHED,
}

enum class ArticleEvent {
    AUTHOR_SUBMIT,
    TE_APPROVE,
    TE_REJECT,
    FPE_APPROVE,
    FPE_REJECT,
}