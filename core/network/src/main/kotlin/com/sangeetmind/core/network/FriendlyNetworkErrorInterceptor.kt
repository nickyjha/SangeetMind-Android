package com.sangeetmind.core.network

import android.content.Context
import com.sangeetmind.core.common.language.LanguageManager
import com.sangeetmind.core.common.language.withAppLanguage
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** The device cannot reach the API at all (no network, DNS failure, connection refused). */
class NetworkUnavailableException(message: String, cause: Throwable) : IOException(message, cause)

/** The API was reached but did not answer in time (cold start, slow link). */
class ServerSlowException(message: String, cause: Throwable) : IOException(message, cause)

/**
 * Turns low-level transport failures into exceptions whose `message` is something a person
 * can act on, in the display language. Every repository surfaces `e.message` to the UI, so
 * this one interceptor replaces "Unable to resolve host ..." everywhere.
 */
class FriendlyNetworkErrorInterceptor(
    private val context: Context,
    private val languageManager: LanguageManager
) : Interceptor {
    private fun text(id: Int): String =
        context.withAppLanguage(languageManager.current).getString(id)

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (e: UnknownHostException) {
            throw NetworkUnavailableException(text(R.string.net_err_offline), e)
        } catch (e: ConnectException) {
            throw NetworkUnavailableException(text(R.string.net_err_offline), e)
        } catch (e: SocketTimeoutException) {
            throw ServerSlowException(text(R.string.net_err_timeout), e)
        }
    }
}
