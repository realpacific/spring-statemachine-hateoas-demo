package com.prashantbarahi.hateoasdemo.statemachine

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.lang.IllegalStateException

/**
 * It is possible for beans of [StateMachineFactory] configured with different [StateMachineConfigurer]
 * to exists (example [ArticleStateMachineBeanConfig]).
 *
 * [StateMachineFactoryProvider] attempts to get those bean by name or default active bean.
 */
@Component
class StateMachineFactoryProvider : ApplicationContextAware {
  private lateinit var context: ApplicationContext
  private val logger = LoggerFactory.getLogger(this::class.java)

  private val stateMachineFactoryBeans: Map<String, StateMachineFactory<*, *>> by lazy {
    this.context.getBeansOfType(StateMachineFactory::class.java)
  }


  override fun setApplicationContext(applicationContext: ApplicationContext) {
    this.context = applicationContext
    validateIfStateMachineKeyAreUnique()
  }

  private fun validateIfStateMachineKeyAreUnique() {
    stateMachineFactoryBeans.forEach { (beanName, factory) ->
      logger.debug(beanName, factory.identifier)
    }
    val beans = stateMachineFactoryBeans.values
    val identifiers = beans.map { it.identifier }
    if (identifiers.distinct().size < beans.size) {
      throw IllegalStateException("StateMachineFactory identifier must be different")
    }
  }

  private fun <S : Enum<S>, E : Enum<E>> cast(bean: StateMachineFactory<*, *>): StateMachineFactory<S, E> {
    return bean as StateMachineFactory<S, E>
  }

  /**
   * @return the [StateMachineFactory] with name [beanName]
   */
  fun <S : Enum<S>, E : Enum<E>> getStateMachineFactory(beanName: String): StateMachineFactory<S, E> {
    return stateMachineFactoryBeans[beanName]?.let(::cast)
        ?: throw RuntimeException("No StateMachineFactory exists with name $beanName")
  }

  /**
   * @return the [StateMachineFactory] with identifier [identifier]
   */
  fun <S : Enum<S>, E : Enum<E>> getStateMachineFactoryOrNull(identifier: StateMachineKey): StateMachineFactory<S, E>? {
    return stateMachineFactoryBeans.values.find { it.identifier.key == identifier.key }?.let(::cast)
  }

  fun <S : Enum<S>, E : Enum<E>> getStateMachineFactory(identifier: StateMachineKey): StateMachineFactory<S, E> {
    return getStateMachineFactoryOrNull(identifier)!!
  }

  /**
   * Get the default [StateMachineFactory] bean
   * (uses @[Primary] annotated bean if there are multiple beans of that type)
   *
   * @return
   */
  fun <S : Enum<S>, E : Enum<E>> getDefaultStateMachine(): StateMachineFactory<S, E> {

    // if only a single bean of such type exists, then return that
    if (stateMachineFactoryBeans.size == 1) return stateMachineFactoryBeans.values.first().let(::cast)

    val primaryBeans = this.context.getBeansWithAnnotation(Primary::class.java)

    // intersection of bean annotated with @Primary and of type StateMachineFactory
    // returns the default StateMachineFactory bean
    val beanName = primaryBeans.keys.intersect(stateMachineFactoryBeans.keys).firstOrNull()
        ?: throw RuntimeException("No @Primary annotated bean found for StateMachineFactory")
    return getStateMachineFactory(beanName)
  }
}