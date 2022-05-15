/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yourcompany.articlereviewworkflow.statemachine

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
    validateAllStateMachineKeysAreUnique()
  }

  private fun validateAllStateMachineKeysAreUnique() {
    stateMachineFactoryBeans.forEach { (beanName, factory) ->
      logger.debug(beanName, factory.identifier)
    }
    val beans = stateMachineFactoryBeans.values
    val identifiers = beans.map { it.identifier }
    if (identifiers.distinct().size < beans.size) {
      throw IllegalStateException("StateMachineFactory identifier must be different")
    }
  }

  private fun <S : Enum<S>, E : Enum<E>> cast(
    bean: StateMachineFactory<*, *>
  ): StateMachineFactory<S, E> {
    return bean as StateMachineFactory<S, E>
  }

  /**
   * @return the [StateMachineFactory] with name [beanName]
   */
  fun <S : Enum<S>, E : Enum<E>> getStateMachineFactory(
    beanName: String
  ): StateMachineFactory<S, E> {
    return stateMachineFactoryBeans[beanName]?.let(::cast)
      ?: throw RuntimeException("No StateMachineFactory exists with name $beanName")
  }

  /**
   * @return the [StateMachineFactory] with identifier [identifier]
   */
  fun <S : Enum<S>, E : Enum<E>> getStateMachineFactoryOrNull(
    identifier: StateMachineKey
  ): StateMachineFactory<S, E>? {
    return stateMachineFactoryBeans.values.find { it.identifier.key == identifier.key }?.let(::cast)
  }

  fun <S : Enum<S>, E : Enum<E>> getStateMachineFactory(
    identifier: StateMachineKey
  ): StateMachineFactory<S, E> {
    return getStateMachineFactoryOrNull(identifier)!!
  }

  /**
   * Get the default [StateMachineFactory] bean
   * (uses @[Primary] annotated bean if there are multiple beans of that type)
   *
   * @return
   */
  fun <S : Enum<S>, E : Enum<E>> getDefaultStateMachineFactory(): StateMachineFactory<S, E> {

    // if only a single bean of such type exists, then return that
    if (stateMachineFactoryBeans.size == 1)
      return stateMachineFactoryBeans.values.first().let(::cast)

    val primaryBeans = this.context.getBeansWithAnnotation(Primary::class.java)

    // intersection of bean annotated with @Primary and of type StateMachineFactory
    // returns the default StateMachineFactory bean
    val beanName = primaryBeans.keys.intersect(stateMachineFactoryBeans.keys).firstOrNull()
      ?: throw RuntimeException("No @Primary annotated bean found for StateMachineFactory")
    return getStateMachineFactory(beanName)
  }
}