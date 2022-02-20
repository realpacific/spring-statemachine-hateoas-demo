package com.prashantbarahi.hateoasdemo

import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder

fun ArticleEvent.withMessage(id: Long): Message<ArticleEvent> {
    return MessageBuilder.withPayload(this).setHeader("articleId", id).build()
}