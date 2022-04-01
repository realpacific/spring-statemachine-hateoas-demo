//package com.prashantbarahi.hateoasdemo
//
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.statemachine.config.EnableStateMachineFactory
//import org.springframework.statemachine.config.StateMachineConfigurerAdapter
//import org.springframework.statemachine.config.StateMachineFactory
//import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer
//import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
//import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer
//import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor
//import org.springframework.statemachine.data.jpa.JpaStateMachineRepository
//import org.springframework.statemachine.listener.StateMachineListenerAdapter
//import org.springframework.statemachine.persist.StateMachineRuntimePersister
//import org.springframework.statemachine.service.DefaultStateMachineService
//import org.springframework.statemachine.service.StateMachineService
//import org.springframework.statemachine.state.State
//import java.util.*
//
//@Configuration
//class ArticleStateMachineConfig {
//
//    @Configuration
//    class StateMachinePersistenceConfig {
//        @Bean
//        fun stateMachineRuntimePersister(
//            jpaStateMachineRepository: JpaStateMachineRepository
//        ): JpaPersistingStateMachineInterceptor<ArticleState, ArticleEvent, String> {
//            return JpaPersistingStateMachineInterceptor(jpaStateMachineRepository)
//        }
//
//        @Bean
//        fun stateMachineService(
//            stateMachineFactory: StateMachineFactory<ArticleState, ArticleEvent>,
//            stateMachineRuntimePersister: StateMachineRuntimePersister<ArticleState, ArticleEvent, String>
//        ): StateMachineService<ArticleState, ArticleEvent> {
//            return DefaultStateMachineService(stateMachineFactory, stateMachineRuntimePersister)
//        }
//    }
//
//
//    @Configuration
//    @EnableStateMachineFactory
//    class MachineConfig : StateMachineConfigurerAdapter<ArticleState, ArticleEvent>() {
//
//        @Autowired
//        private lateinit var stateMachineRuntimePersister: StateMachineRuntimePersister<ArticleState, ArticleEvent, String>
//
//        override fun configure(config: StateMachineConfigurationConfigurer<ArticleState, ArticleEvent>) {
//            config.withPersistence()
//                .runtimePersister(stateMachineRuntimePersister)
//                .and()
//                .withConfiguration()
//                .autoStartup(true)
//                .listener(object : StateMachineListenerAdapter<ArticleState, ArticleEvent>() {
//                    override fun stateChanged(
//                        from: State<ArticleState, ArticleEvent>?,
//                        to: State<ArticleState, ArticleEvent>?
//                    ) {
//                        println("${from?.id} –––> ${to?.id}")
//                    }
//                })
//        }
//
//        override fun configure(states: StateMachineStateConfigurer<ArticleState, ArticleEvent>) {
//            states
//                .withStates()
//                .initial(ArticleState.DRAFT)
//                .states(EnumSet.allOf(ArticleState::class.java))
//                .end(ArticleState.PUBLISHED)
//        }
//
//        override fun configure(transitions: StateMachineTransitionConfigurer<ArticleState, ArticleEvent>) {
//            transitions
//
//                // ---- AUTHOR ------
//                .withExternal()
//                .source(ArticleState.DRAFT)
//                .event(ArticleEvent.AUTHOR_SUBMIT)
//                .target(ArticleState.AUTHOR_SUBMITTED)
//                .and()
//
//                // ---- TE ------
//                .withExternal()
//                .source(ArticleState.AUTHOR_SUBMITTED)
//                .event(ArticleEvent.TE_APPROVE)
//                .target(ArticleState.TE_APPROVED)
//                .and()
//
//                .withExternal()
//                .source(ArticleState.AUTHOR_SUBMITTED)
//                .event(ArticleEvent.TE_REJECT)
//                .target(ArticleState.DRAFT)
//                .and()
//
//                // ---- FPE ------
//                .withExternal()
//                .source(ArticleState.TE_APPROVED)
//                .event(ArticleEvent.FPE_REJECT)
//                .target(ArticleState.DRAFT)
//                .and()
//
//                .withExternal()
//                .source(ArticleState.TE_APPROVED)
//                .event(ArticleEvent.FPE_APPROVE)
//                .target(ArticleState.PUBLISHED)
//
//        }
//    }
//}