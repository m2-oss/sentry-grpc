package ru.m2.sentry

import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import io.grpc.Attributes
import io.grpc.Context
import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import io.grpc.internal.SerializingExecutor
import io.sentry.Sentry
import java.util.concurrent.ExecutionException

class SentryServerInterceptor : ServerInterceptor {
    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        return SentryGprcExceptionListener(
            next.startCall(SerializingServerCall(call), headers),
            call,
            headers
        )
    }
}

private class SentryGprcExceptionListener<ReqT, RespT>(
    delegate: ServerCall.Listener<ReqT>?,
    call: ServerCall<ReqT, RespT>,
    headers: Metadata
) : SimpleForwardingServerCallListener<ReqT>(delegate) {
    private val extraInfoCollector = ExtraInfoCollector()

    init {
        extraInfoCollector.recordMetadata(headers)
        extraInfoCollector.recordCallData(call)
    }

    override fun onMessage(message: ReqT) {
        try {
            extraInfoCollector.recordMessage(message)
            extraInfoCollector.addEvent("onMessageStart")
            super.onMessage(message)
            extraInfoCollector.addEvent("onMessageFinish")
        } catch (e: Exception) {
            extraInfoCollector.addEvent("onMessageFailed")
            logException(e)
            throw e
        }
    }

    override fun onCancel() {
        try {
            extraInfoCollector.addEvent("onCancel")
            super.onCancel()
        } catch (e: Exception) {
            extraInfoCollector.addEvent("onCancelFailed")
            logException(e)
            throw e
        }
    }

    override fun onComplete() {
        try {
            extraInfoCollector.addEvent("onComplete")
            super.onComplete()
        } catch (e: Exception) {
            extraInfoCollector.addEvent("onCompleteFailed")
            logException(e)
            throw e
        }
    }

    override fun onHalfClose() {
        try {
            extraInfoCollector.addEvent("onHalfClose")
            super.onHalfClose()
        } catch (e: Exception) {
            extraInfoCollector.addEvent("onHalfCloseFailed")
            logException(e)
            throw e
        }
    }

    override fun onReady() {
        try {
            extraInfoCollector.addEvent("onReady")
            super.onReady()
        } catch (e: Exception) {
            extraInfoCollector.addEvent("onReadyFailed")
            logException(e)
            throw e
        }
    }

    private fun logException(ex: Exception) {
        extraInfoCollector.recordContext(Context.current())
        Sentry.captureException(ex)
    }
}

/**
 * A [ServerCall] that wraps around a non thread safe delegate and provides thread safe
 * access by serializing everything on an executor.
 */
private class SerializingServerCall<ReqT, RespT>(delegate: ServerCall<ReqT, RespT>?) :
    ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(delegate) {
    private val serializingExecutor = SerializingExecutor(MoreExecutors.directExecutor())
    private var closeCalled = false
    override fun sendMessage(message: RespT) {
        serializingExecutor.execute { super@SerializingServerCall.sendMessage(message) }
    }

    override fun request(numMessages: Int) {
        serializingExecutor.execute { super@SerializingServerCall.request(numMessages) }
    }

    override fun sendHeaders(headers: Metadata) {
        serializingExecutor.execute { super@SerializingServerCall.sendHeaders(headers) }
    }

    override fun close(status: Status, trailers: Metadata) {
        serializingExecutor.execute {
            if (!closeCalled) {
                closeCalled = true
                super@SerializingServerCall.close(status, trailers)
            }
        }
    }

    override fun isReady(): Boolean {
        val retVal = SettableFuture.create<Boolean>()
        serializingExecutor.execute { retVal.set(super@SerializingServerCall.isReady()) }
        return try {
            retVal.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(ERROR_MSG, e)
        } catch (e: ExecutionException) {
            throw RuntimeException(ERROR_MSG, e)
        }
    }

    override fun isCancelled(): Boolean {
        val retVal = SettableFuture.create<Boolean>()
        serializingExecutor.execute { retVal.set(super@SerializingServerCall.isCancelled()) }
        return try {
            retVal.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(ERROR_MSG, e)
        } catch (e: ExecutionException) {
            throw RuntimeException(ERROR_MSG, e)
        }
    }

    override fun setMessageCompression(enabled: Boolean) {
        serializingExecutor.execute { super@SerializingServerCall.setMessageCompression(enabled) }
    }

    override fun setCompression(compressor: String) {
        serializingExecutor.execute { super@SerializingServerCall.setCompression(compressor) }
    }

    override fun getAttributes(): Attributes {
        val retVal = SettableFuture.create<Attributes>()
        serializingExecutor.execute { retVal.set(super@SerializingServerCall.getAttributes()) }
        return try {
            retVal.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(ERROR_MSG, e)
        } catch (e: ExecutionException) {
            throw RuntimeException(ERROR_MSG, e)
        }
    }

    override fun getAuthority(): String? {
        val retVal = SettableFuture.create<String>()
        serializingExecutor.execute { retVal.set(super@SerializingServerCall.getAuthority()) }
        return try {
            retVal.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(ERROR_MSG, e)
        } catch (e: ExecutionException) {
            throw RuntimeException(ERROR_MSG, e)
        }
    }

    companion object {
        private const val ERROR_MSG = "Encountered error during serialized access"
    }
}
