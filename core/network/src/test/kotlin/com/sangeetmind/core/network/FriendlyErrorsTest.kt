package com.sangeetmind.core.network

import com.squareup.moshi.JsonDataException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.UnknownHostException

class FriendlyErrorsTest {
    private fun http(code: Int) = HttpException(
        Response.error<Any>(code, "{\"detail\":\"boom\"}".toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun httpCodesMapToKinds() {
        assertEquals(FriendlyErrorKind.AUTH, classifyError(http(401)))
        assertEquals(FriendlyErrorKind.CLIENT, classifyError(http(400)))
        assertEquals(FriendlyErrorKind.CLIENT, classifyError(http(404)))
        assertEquals(FriendlyErrorKind.RATE_LIMIT, classifyError(http(429)))
        assertEquals(FriendlyErrorKind.SERVER, classifyError(http(500)))
        assertEquals(FriendlyErrorKind.SERVER, classifyError(http(503)))
    }

    @Test
    fun transportKeepsInterceptorMessage() {
        val e = NetworkUnavailableException("offline", UnknownHostException())
        assertEquals(FriendlyErrorKind.TRANSPORT, classifyError(e))
        assertEquals(FriendlyErrorKind.TRANSPORT, classifyError(ServerSlowException("slow", IOException())))
    }

    @Test
    fun parseAndOtherErrorsAreGeneric() {
        assertEquals(FriendlyErrorKind.GENERIC, classifyError(JsonDataException("Required value 'x' missing")))
        assertEquals(FriendlyErrorKind.GENERIC, classifyError(IOException("unexpected end of stream")))
        assertEquals(FriendlyErrorKind.GENERIC, classifyError(IllegalStateException("x")))
    }
}
