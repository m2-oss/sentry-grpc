package ru.m2.sentry

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.filter.ThresholdFilter
import io.sentry.logback.SentryAppender
import org.lognet.springboot.grpc.GRpcGlobalInterceptor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener


@Configuration
open class SentryConfiguration {

    @Bean
    @GRpcGlobalInterceptor
    open fun sentryServerInterceptor() = SentryServerInterceptor()

    @Bean
    open fun configurationEventListener() = ConfigurationEventListener()

    inner class ConfigurationEventListener {
        @EventListener
        fun configLogback(event: ContextRefreshedEvent) = with(LoggerFactory.getILoggerFactory() as LoggerContext) {
            this@with.getLogger(Logger.ROOT_LOGGER_NAME).apply {
                getAppender("Sentry") ?: addAppender(
                    SentryAppender().apply {
                        context = this@with
                        name = "Sentry"
                        addFilter(
                            ThresholdFilter().apply {
                                context = this@with
                                setLevel("WARN")
                                start()
                            }
                        )
                        start()
                    }
                )
            }
        }
    }
}
