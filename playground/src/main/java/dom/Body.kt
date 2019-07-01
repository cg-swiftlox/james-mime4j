package playground

import io.reactivex.Maybe
import io.reactivex.Observable
import org.apache.james.mime4j.stream.BodyDescriptor
import java.io.InputStream

interface Body {
    fun getText(): Maybe<Attachment>
    fun getHtml(): Maybe<HtmlAttachment>
    fun getAttachments(): Observable<Attachment>

    fun setAttachment(bd: BodyDescriptor?, inStream: InputStream?)
}