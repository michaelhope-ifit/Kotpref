package com.chibatching.kotpref.moshipref

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.KotprefModel
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token.NULL
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.io.IOException
import java.util.Arrays
import java.util.Calendar
import java.util.Date

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class MoshiSupportTest(private val commitAllProperties: Boolean) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "commitAllProperties = {0}")
        fun data(): Collection<Array<out Any>> {
            return Arrays.asList(arrayOf(false), arrayOf(true))
        }

        fun createDefaultContent(): Content =
            Content("default title", "contents write here", createDate(2017, 1, 5))

        fun createDate(year: Int, month: Int, day: Int): Date =
            Calendar.getInstance().apply {
                set(year, month, day)
                set(Calendar.HOUR, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
    }

    class Example(private val commitAllProperties: Boolean) :
        KotprefModel(ApplicationProvider.getApplicationContext<Context>()) {
        override val commitAllPropertiesByDefault: Boolean
            get() = commitAllProperties

        var content by moshiPref(createDefaultContent())

        var list: List<String> by moshiPref(emptyList<String>())

        var nullableContent: Content? by moshiNullablePref<Content>()
    }

    private lateinit var example: Example
    private lateinit var pref: SharedPreferences

    @Before
    fun setUp() {
        Kotpref.moshi = Moshi.Builder()
            .add(DateJsonAdapter())
            .add(ContentJsonAdapter())
            .build()
        example = Example(commitAllProperties)

        pref = example.preferences
        pref.edit().clear().commit()
    }

    @After
    fun tearDown() {
        example.clear()
    }

    @Test
    fun moshiPrefDefaultIsDefined() {
        assertThat(example.content).isEqualTo(createDefaultContent())
    }

    @Test
    fun setMoshiPrefSetCausePreferenceUpdate() {
        val title = "new title"
        val body = "this is new content"
        val createdAt = createDate(2017, 1, 25)
        example.content = Content(title, body, createdAt)
        assertThat(example.content)
            .isEqualTo(
                Content(
                    title,
                    body,
                    createdAt
                )
            )
        assertThat(example.content)
            .isEqualTo(
                Kotpref.moshi?.adapter(Content::class.java)?.fromJson(
                    pref.getString("content", "")!!
                )
            )
    }

    @Test
    fun moshiNullablePrefDefaultIsNull() {
        assertThat(example.nullableContent).isNull()
    }

    @Test
    fun moshiNullablePrefCausePreferenceUpdate() {
        val title = "nullable content"
        val body = "this is not null"
        val createdAt = createDate(2017, 1, 20)
        example.nullableContent = Content(title, body, createdAt)
        assertThat(example.nullableContent)
            .isEqualTo(
                Content(
                    title,
                    body,
                    createdAt
                )
            )
        assertThat(example.nullableContent)
            .isEqualTo(
                Kotpref.moshi?.adapter(Content::class.java)?.fromJson(
                    pref.getString(
                        "nullableContent",
                        ""
                    )!!
                )
            )
    }

    @Test
    fun moshiNullablePrefSetNull() {

        fun setNull() {
            example.nullableContent = null
        }
        setNull()
        assertThat(example.nullableContent)
            .isEqualTo(
                Kotpref.moshi?.adapter(Content::class.java)?.fromJson(
                    pref.getString(
                        "nullableContent",
                        ""
                    )!!
                )
            )
        assertThat(example.nullableContent).isNull()
    }

    @Test
    fun moshiGenericTypeTest() {
        example.list = listOf("moshi", "generic", "type")
        val theType = Types.newParameterizedType(List::class.java, String::class.java)
        val result = Kotpref.moshi?.adapter<List<String>>(theType)?.fromJson(
            pref.getString("list", "")!!
        )
        assertThat(example.list)
            .containsExactlyElementsIn(result)
            .inOrder()
    }
}

private class DateJsonAdapter: JsonAdapter<Date>() {
    @Synchronized
    @FromJson
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Date? {
        if (reader.peek() == NULL) {
            return reader.nextNull()
        }
        val string = reader.nextString()
        return string.parseIsoDate()
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val string = value.formatIsoDate()
            writer.value(string)
        }
    }
}
