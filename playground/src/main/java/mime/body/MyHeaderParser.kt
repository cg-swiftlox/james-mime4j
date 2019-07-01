package playground.mime.body

import org.apache.james.mime4j.MimeException
import org.apache.james.mime4j.MimeIOException
import org.apache.james.mime4j.codec.DecodeMonitor
import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.dom.field.ParsedField
import org.apache.james.mime4j.field.DefaultFieldParser
import org.apache.james.mime4j.field.LenientFieldParser
import org.apache.james.mime4j.field.contenttype.parser.ContentTypeParser
import org.apache.james.mime4j.message.HeaderImpl
import org.apache.james.mime4j.parser.AbstractContentHandler
import org.apache.james.mime4j.parser.MimeStreamParser
import org.apache.james.mime4j.stream.Field
import org.apache.james.mime4j.stream.MimeConfig
import java.io.*
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class MyHeaderParser {
    var fields: ArrayList<MyField> = ArrayList()
    var curState: FieldState = FieldState.FIELD_START
    var curField: MyField? = null

    lateinit var name: String
    lateinit var body: String

    fun parseField(line: String) {
        when (curState) {
            FieldState.FIELD_START -> {
                val parts = line.split(":")
                when (parts[0]) {
                    FieldTypes.CONTENT_TYPE -> {
                        name = parts[0]

                        val bodySplit = parts[1].split(";")
                        body = bodySplit[0] + ";"
                        if(bodySplit[1].isBlank()){
                            curState = FieldState.FIELD_IN_PROCESS
                        } else {
                            val f = parseContentType()
                            fields.add(f)
                        }
                    }
                    FieldTypes.CONTENT_TRANSFER_ENCODING -> {
                        val f = MyField()
                        f.name = parts[0]
                        f.body = parts[1]
                        fields.add(f)
                    }
                }
            }
            FieldState.FIELD_IN_PROCESS -> {
                body += line
                when (name) {
                    FieldTypes.CONTENT_TYPE -> {
                        val f = parseContentType()
                        fields.add(f)
                    }
                }
                curState = FieldState.FIELD_START
            }
        }
    }

    private fun parseContentType(): MyField {
        val parser = ContentTypeParser(StringReader(body))
        parser.parseAll()
//        val mediaType = parser.type
//        val subType = parser.subType
//        val paramNames = parser.paramNames
//        val paramValues = parser.paramValues
        return MyField()
    }

    @Throws(IOException::class, MimeIOException::class)
    fun parseHeader(line: String): Header {
        val cfg = MimeConfig.DEFAULT
        val mon = DecodeMonitor.SILENT
        val fp = LenientFieldParser.getParser()
        val header = HeaderImpl()
        val parser = MimeStreamParser(cfg, mon, null)
        parser.setContentHandler(object : AbstractContentHandler() {
            override fun endHeader() {
                parser.stop()
            }

            @Throws(MimeException::class)
            override fun field(field: Field) {
                val parsedField: ParsedField
                if (field is ParsedField) {
                    parsedField = field
                } else {
                    parsedField = fp.parse(field, mon)
                }
                header.addField(parsedField)
            }
        })
        try {
            val inputStream: InputStream = ByteArrayInputStream(line.toByteArray(StandardCharsets.UTF_8))
            parser.parse(inputStream)
        } catch (ex: MimeException) {
            throw MimeIOException(ex)
        }

        return header
    }
}

class FieldTypes {
    companion object{
        const val CONTENT_TYPE = "Content-Type"
        const val CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding"
    }
}

enum class FieldState {
    FIELD_START,
    FIELD_IN_PROCESS
}