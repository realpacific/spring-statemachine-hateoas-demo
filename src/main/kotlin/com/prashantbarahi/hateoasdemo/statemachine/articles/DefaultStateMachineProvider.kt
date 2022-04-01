package com.prashantbarahi.hateoasdemo.statemachine.articles

import org.springframework.stereotype.Component

@Component
class DefaultStateMachineProvider(private val factory: ArticleStateMachineFactoryOfFactories) {

}