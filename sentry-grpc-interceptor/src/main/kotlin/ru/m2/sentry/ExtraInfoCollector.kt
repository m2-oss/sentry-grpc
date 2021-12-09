package ru.m2.sentry

import io.grpc.Context
import io.grpc.Metadata.ASCII_STRING_MARSHALLER
import io.grpc.Metadata.Key
import io.grpc.ServerCall
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.protocol.User
import org.slf4j.MDC
import java.util.concurrent.TimeUnit

class ExtraInfoCollector() {

    fun recordMessage(message: Any?) {
        record("message", message?.toString() ?: "")
    }

    fun addEvent(name: String) {
        recordBreadcrumb(name)
    }

    fun recordMetadata(metadata: io.grpc.Metadata) {
        for (key in metadata.keys()) {
            record(key, metadata.getAll(Key.of(key, ASCII_STRING_MARSHALLER))?.joinToString())
            if (key == "x-b3-traceid") {
                metadata.getAll(Key.of(key, ASCII_STRING_MARSHALLER))?.joinToString()?.let {
                    recordTag("x-trace-id", it)
                }
            }
        }
        recordUser(metadata)
    }

    fun <ReqT, RespT> recordCallData(call: ServerCall<ReqT, RespT>) {
        record("call-addressed", call.authority)
        with(call.methodDescriptor) {
            record("service", this.serviceName)
            record("method", this.fullMethodName)
        }

        val attributes = call.attributes
        //since there were no point in visibility-restriction attributes content
        //but for some reason they still deprecate this method
        for (key in attributes.keys()) {
            record(key.toString(), attributes[key])
        }
    }

    private fun recordUser(metadata: io.grpc.Metadata) {
        var userId: String? = null
        metadata.getAll(Key.of("x-user-id", ASCII_STRING_MARSHALLER))?.joinToString()?.let {
            userId = it
        }
        var username: String? = null
        metadata.getAll(Key.of("x-username", ASCII_STRING_MARSHALLER))?.joinToString()?.let {
            username = it
        }
        var email: String? = null
        metadata.getAll(Key.of("x-user-email", ASCII_STRING_MARSHALLER))?.joinToString()?.let {
            email = it
        }
        var ip: String? = null
        metadata.getAll(Key.of("x-real-ip", ASCII_STRING_MARSHALLER))?.joinToString()?.let {
            ip = it
        }
        var phone: String? = null
        metadata.getAll(Key.of("x-user-phone", ASCII_STRING_MARSHALLER))?.joinToString()?.let {
            phone = it
        }
        var companyId: String? = null
        metadata.getAll(Key.of("x-user-company-id", ASCII_STRING_MARSHALLER))?.joinToString()?.let {
            companyId = it
        }
        val userData = mutableMapOf<String, String>()
        phone.let { userData["phone"] = it!! }
        companyId.let { userData["companyId"] = it!! }

        val user = User()
        user.id = userId
        user.username = username
        user.ipAddress = ip
        user.email = email
        user.others = userData

        Sentry.configureScope {
            it.user = user
        }
    }

    private fun record(key: String, value: Any?) {
        value.let { v ->
            Sentry.configureScope {
                it.setExtra(key, v.toString())
            }
        }
    }

    private fun recordTag(key: String, value: String) {
        Sentry.configureScope {
            it.setTag(key, value)
        }
    }

    private fun recordBreadcrumb(value: String) {
        Sentry.configureScope { scope ->
            scope.addBreadcrumb(
                Breadcrumb.error(value)
                    .also { it.category =  "rpc call processing"}
            )
        }
    }

    fun recordContext(context: Context) {
        record(
            "deadlineRemainingMs",
            context.deadline?.timeRemaining(TimeUnit.MILLISECONDS)?.toString() ?: "-1"
        )
        Sentry.configureScope { scope ->
            MDC.getCopyOfContextMap()?.forEach { (key, value) -> scope.setExtra(key, value) }
        }
    }
}
