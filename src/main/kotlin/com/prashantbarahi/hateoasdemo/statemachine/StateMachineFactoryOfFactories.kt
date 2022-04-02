package com.prashantbarahi.hateoasdemo.statemachine

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * It is possible for beans of [StateMachineFactory] configured with different [StateMachineStateConfigurer]
 * to exists (example [ArticleStateMachineBeanConfig]).
 *
 * [StateMachineFactoryOfFactories] attempts to get those bean by name or default active bean.
 */
@Component
class StateMachineFactoryOfFactories<S : Enum<S>, E : Enum<E>> : ApplicationContextAware {
    private lateinit var context: ApplicationContext

    protected val beansByType: Map<String, StateMachineFactory<*, *>> by lazy {
        this.context.getBeansOfType(StateMachineFactory::class.java)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context = applicationContext
    }

    /**
     * @return the [StateMachineFactory] with name [beanName]
     */
    fun getStateMachineFactory(beanName: String): StateMachineFactory<S, E> {
        return beansByType[beanName] as? StateMachineFactory<S, E>
            ?: throw RuntimeException("No StateMachineFactory exists with name $beanName")
    }

    /**
     * Get the default [StateMachineFactory] bean
     * (uses @[Primary] annotated bean if there are multiple beans of that type)
     *
     * @return
     */
    fun getDefaultStateMachineAsNameSelfPair(): Pair<String, StateMachineFactory<S, E>> {

        // if only a single bean of such type exists, then use that
        if (beansByType.size == 1) {
            val beanName = beansByType.keys.first()
            return beanName to getStateMachineFactory(beanName)
        }

        val primaryBeans = this.context.getBeansWithAnnotation(Primary::class.java)

        // intersection of bean annotated with @Primary and of type StateMachineFactory
        // returns the default StateMachineFactory bean
        val beanName = primaryBeans.keys.intersect(beansByType.keys).firstOrNull()
            ?: throw RuntimeException("No @Primary annotated bean found for StateMachineFactory")
        return beanName to getStateMachineFactory(beanName)
    }
}