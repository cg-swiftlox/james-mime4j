package playground.dom

import io.reactivex.Maybe
import io.reactivex.Observable
import org.apache.james.mime4j.stream.BodyDescriptor
import playground.Attachment
import playground.Body
import playground.HtmlAttachment
import playground.TextAttachment
import java.io.InputStream

class BodyImpl: Body {

    private lateinit var plainTextBody: TextAttachment
    private lateinit var htmlBody: HtmlAttachment
    private var attachments: ArrayList<Attachment> = ArrayList()

    override fun setAttachment(bd: BodyDescriptor?, inStream: InputStream?) {
        println("${bd?.mimeType} / ${bd?.transferEncoding}")

        when(bd?.mimeType) {
            "text/plain" -> addPlainTextBody(bd, inStream)
            "text/html" -> addHtmlBody(bd, inStream)
            else -> addAttachment(bd, inStream)
        }
    }

    private fun addPlainTextBody(bd: BodyDescriptor?, inStream: InputStream?) {
        plainTextBody = TextAttachment(bd, inStream)
    }

    private fun addHtmlBody(bd: BodyDescriptor?, inStream: InputStream?) {
        htmlBody = HtmlAttachment(bd, inStream)
    }

    private fun addAttachment(bd: BodyDescriptor?, inStream: InputStream?) {
        attachments.add(EmailAttachment(bd, inStream))
    }

    override fun getText(): Maybe<Attachment> {
        throw NotImplementedError("")
    }

    override fun getHtml(): Maybe<HtmlAttachment> {
        throw NotImplementedError("")
    }

    override fun getAttachments(): Observable<Attachment> {
        throw NotImplementedError("")
    }


    private fun isPlainTextType(bd: BodyDescriptor?): Boolean {
        return bd?.mimeType.equals("text/plain")
    }

    private fun isHtmlType(bd: BodyDescriptor?): Boolean {
        return bd?.mimeType.equals("text/html")
    }
}