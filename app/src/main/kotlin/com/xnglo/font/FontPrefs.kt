package com.xnglo.font

import android.content.Context

/**
 * Remembers the user's chosen "default font" across app launches
 * (the "Make this the default font" checkbox in MainActivity). This
 * is separate from whether the Magisk module itself re-applies on
 * boot (see RootFontInstaller's reapplyOnBoot) -- this is just which
 * font the spinner should pre-select next time the app opens.
 */
object FontPrefs {
    private const val PREFS_NAME = "xnglofont_prefs"
    private const val KEY_DEFAULT_FONT_ID = "default-font-id"

    fun getDefaultFontId(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DEFAULT_FONT_ID, null)

    fun setDefaultFontId(context: Context, fontId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEFAULT_FONT_ID, fontId)
            .apply()
    }
}
