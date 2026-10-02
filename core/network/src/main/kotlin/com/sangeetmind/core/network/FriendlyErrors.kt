package com.sangeetmind.core.network

import android.content.Context
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import java.io.IOException

/** What kind of failure an exception represents, independent of display language. */
enum class FriendlyErrorKind { TRANSPORT, AUTH, RATE_LIMIT, CLIENT, SERVER, GENERIC }

/** Pure classification so it can be unit-tested without Android resources. */
fun classifyError(e: Throwable): FriendlyErrorKind = when (e) {
    is NetworkUnavailableException, is ServerSlowException -> FriendlyErrorKind.TRANSPORT
    is HttpException -> when (val code = e.code()) {
        401 -> FriendlyErrorKind.AUTH
        429 -> FriendlyErrorKind.RATE_LIMIT
        in 400..499 -> FriendlyErrorKind.CLIENT
        in 500..599 -> FriendlyErrorKind.SERVER
        else -> if (code >= 600) FriendlyErrorKind.SERVER else FriendlyErrorKind.GENERIC
    }
    is JsonDataException, is JsonEncodingException -> FriendlyErrorKind.GENERIC
    is IOException -> FriendlyErrorKind.GENERIC
    else -> FriendlyErrorKind.GENERIC
}

/**
 * A message a person can act on for any repository failure, in [context]'s language
 * (pass a context already wrapped with `withAppLanguage`). Offline/timeout keep the
 * text set by [FriendlyNetworkErrorInterceptor]; HTTP and parse errors never leak
 * "HTTP 500 Internal Server Error" or Moshi stack details. [fallback] is used for
 * unexpected non-network exceptions (e.g. "Couldn't load panchang").
 */
fun friendlyErrorMessage(e: Throwable, context: Context, fallback: String? = null): String =
    when (classifyError(e)) {
        FriendlyErrorKind.TRANSPORT -> e.message?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.net_err_offline)
        FriendlyErrorKind.AUTH -> context.getString(R.string.net_err_sign_in_again)
        FriendlyErrorKind.RATE_LIMIT -> context.getString(R.string.net_err_rate_limited)
        FriendlyErrorKind.CLIENT -> context.getString(R.string.net_err_bad_request)
        FriendlyErrorKind.SERVER -> context.getString(R.string.net_err_server)
        FriendlyErrorKind.GENERIC -> fallback?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.net_err_generic)
    }
