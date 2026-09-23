package com.sangeetmind.core.common.language

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.view.ContextThemeWrapper

/**
 * A context whose resources resolve in [language], wrapping (not replacing) the receiver.
 *
 * It is a [ContextThemeWrapper] around the original so anything that unwraps to find the
 * Activity (Hilt's `hiltViewModel()`, [findActivity]) still succeeds; only `resources` and
 * `configuration` differ. Compose's `stringResource` reads `LocalContext.current.resources`,
 * so providing this as `LocalContext` re-resolves every string in the tree.
 */
fun Context.withAppLanguage(language: AppLanguage): Context {
    val config = Configuration(resources.configuration).apply {
        setLocale(language.locale)
        setLayoutDirection(language.locale)
    }
    return ContextThemeWrapper(this, 0).apply { applyOverrideConfiguration(config) }
}

/** Unwrap [ContextWrapper]s (including [withAppLanguage]'s) down to the hosting Activity. */
fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
