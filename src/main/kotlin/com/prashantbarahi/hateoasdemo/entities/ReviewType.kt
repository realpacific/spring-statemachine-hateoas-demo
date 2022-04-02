package com.prashantbarahi.hateoasdemo.entities

import com.prashantbarahi.hateoasdemo.statemachine.StateMachineKey

enum class ReviewType : StateMachineKey {

    THREE_LEVEL_WORKFLOW {
        override val key = "THREE_LEVEL_REVIEW_WORKFLOW"
    },

    FOUR_LEVEL_WORKFLOW {
        override val key = "FOUR_LEVEL_REVIEW_WORKFLOW"
    }
}