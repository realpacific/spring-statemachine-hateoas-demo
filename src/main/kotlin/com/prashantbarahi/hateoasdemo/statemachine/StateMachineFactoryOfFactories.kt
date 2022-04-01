package com.prashantbarahi.hateoasdemo.statemachine

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class StateMachineFactoryOfFactories<S : Enum<S>, E : Enum<E>> : ApplicationContextAware {
    private lateinit var context: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }

    fun getStateMachineFactory(name: String): StateMachineFactory<S, E> {
        val beans = this.context.getBeansOfType(StateMachineFactory::class.java)
        return beans[name]!! as StateMachineFactory<S, E>
    }
}