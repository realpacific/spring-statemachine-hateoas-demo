package com.prashantbarahi.hateoasdemo.statemachine

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Primary
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

    fun getDefaultStateMachineAsNameSelfPair(): Pair<String, StateMachineFactory<S, E>> {
        val primaryBeans = this.context.getBeansWithAnnotation(Primary::class.java)
        val beansByType = this.context.getBeansOfType(StateMachineFactory::class.java)
        val beanName = primaryBeans.keys.intersect(beansByType.keys).firstOrNull()
            ?: throw RuntimeException("No @Primary annotated bean found for StateMachineFactory")
        return beanName to primaryBeans[beanName]!! as StateMachineFactory<S, E>
    }

    fun getDefaultStateMachineFactory(): StateMachineFactory<S, E> {
        val (_, machine) = getDefaultStateMachineAsNameSelfPair()
        return machine
    }
}