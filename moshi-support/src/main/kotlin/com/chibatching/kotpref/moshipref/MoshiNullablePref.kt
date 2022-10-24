package com.chibatching.kotpref.moshipref

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.chibatching.kotpref.execute
import com.chibatching.kotpref.pref.AbstractPref
import com.squareup.moshi.JsonAdapter
import kotlin.reflect.KProperty

public class MoshiNullablePref<T : Any> constructor(
    private val adapter: JsonAdapter<T>,
    private val default: (() -> T?),
    override val key: String?,
    private val commitByDefault: Boolean
) : AbstractPref<T?>() {

    override fun getFromPreference(property: KProperty<*>, preference: SharedPreferences): T? {
        return preference.getString(preferenceKey, null)?.let { json ->
            deserializeFromJson(json) ?: default.invoke()
        }
    }

    @SuppressLint("CommitPrefEdits")
    override fun setToPreference(property: KProperty<*>, value: T?, preference: SharedPreferences) {
        serializeToJson(value).let { json ->
            preference.edit().putString(preferenceKey, json).execute(commitByDefault)
        }
    }

    override fun setToEditor(property: KProperty<*>, value: T?, editor: SharedPreferences.Editor) {
        serializeToJson(value).let { json ->
            editor.putString(preferenceKey, json)
        }
    }

    private fun serializeToJson(value: T?): String? {
        return adapter.let {
            if (it == null) throw IllegalStateException("Moshi has not been set to Kotpref")

            it.toJson(value)
        }
    }

    private fun deserializeFromJson(json: String): T? {
        return adapter.let {
            if (it == null) throw IllegalStateException("Moshi has not been set to Kotpref")

            it.fromJson(json)
        }
    }
}
