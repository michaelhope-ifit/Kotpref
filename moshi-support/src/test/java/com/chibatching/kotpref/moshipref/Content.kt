package com.chibatching.kotpref.moshipref

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.io.IOException
import java.util.Date

@JsonClass(generateAdapter = false)
internal data class Content(

    @Json(name = "title")
    var title: String = "",

    @Json(name = "body")
    var body: String = "",

    @Json(name = "created_at")
    var createdAt: Date = Date()
)

internal class ContentJsonAdapter: JsonAdapter<Content>() {

    val TITLE_KEY = "title"
    val BODY_KEY = "body"
    val CREATED_AT_KEY = "createdAt"

    @Synchronized
    @FromJson
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Content? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        val json = reader.nextString()
        val content = Content()
        val jsonArray = json.split(",")
        for (item in jsonArray) {
            val itemSplit = item.split(":", limit = 2)
            val key = itemSplit[0]
            val value = itemSplit[1]
            when (key) {
                TITLE_KEY -> {
                    content.title = value
                }
                BODY_KEY -> {
                    content.body = value
                }
                CREATED_AT_KEY -> {
                    content.createdAt = value.parseIsoDate()
                }
                else -> continue
            }
        }

        return content
    }

    @Synchronized
    @ToJson
    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Content?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val json = "$TITLE_KEY:${value.title},$BODY_KEY:${value.body},$CREATED_AT_KEY:${value.createdAt.formatIsoDate()}"
            writer.value(json)
        }
    }
}