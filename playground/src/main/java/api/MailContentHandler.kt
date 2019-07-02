package api

import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.message.HeaderImpl
import org.apache.james.mime4j.parser.ContentHandler
import org.apache.james.mime4j.stream.BodyDescriptor
import org.apache.james.mime4j.stream.Field
import java.io.InputStream

class MailContentHandler: ContentHandler {

    val header: Header = HeaderImpl()

    var level = 0

    override fun startMessage() {
        println(getSpacing() + "startMessage()")
        level ++
    }

    override fun endMessage() {
        level --
        println(getSpacing() + "endMessage()")
    }

    override fun startBodyPart() {
        println(getSpacing() + "startBodyPart()")
        level ++
    }

    override fun endBodyPart() {
        level --
        println(getSpacing() + "endBodyPart()")
    }

    override fun startHeader() {
        println(getSpacing() + "startHeader()")
        level ++
    }

    override fun field(rawField: Field?) {
        println(getSpacing() + "field(${rawField?.name} - ${rawField?.body})")
        header.addField(rawField)
    }

    override fun endHeader() {
        level --
        println(getSpacing() + "endHeader()")
    }

    override fun preamble(`is`: InputStream?) {
        println(getSpacing() + "preamble(${`is`})")
    }

    override fun epilogue(`is`: InputStream?) {
        println(getSpacing() + "epilogue(${`is`})")
    }

    override fun startMultipart(bd: BodyDescriptor?) {
        println(getSpacing() + "startMultipart(${bd})")
        level ++
    }

    override fun endMultipart() {
        level --
        println(getSpacing() + "endMultipart()")
    }

    override fun body(bd: BodyDescriptor?, inStream: InputStream?) {
        println(getSpacing() + "body(${bd} - ")//${`is`})")
        print(getSpacing() + "   ")
//        body.setAttachment(bd, inStream)
    }

    override fun raw(`is`: InputStream?) {
        println(getSpacing() + "raw(${`is`})")
    }

    fun getSpacing(): String {
        var spacing = ""
        for(i in 0 .. level)
            spacing += "   "
        return spacing
    }
}