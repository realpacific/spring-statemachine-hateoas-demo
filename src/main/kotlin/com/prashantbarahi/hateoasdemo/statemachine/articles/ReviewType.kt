package com.prashantbarahi.hateoasdemo.statemachine.articles

import com.prashantbarahi.hateoasdemo.statemachine.StateMachineKey

enum class ReviewType : StateMachineKey {

  THREE_LEVEL_WORKFLOW {
    override val key = this.name
  },

  FOUR_LEVEL_WORKFLOW {
    override val key = this.name
  }
}