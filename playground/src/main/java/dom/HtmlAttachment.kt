package playground

import org.apache.james.mime4j.stream.BodyDescriptor
import java.io.InputStream

class HtmlAttachment(bd: BodyDescriptor?, inStream: InputStream?): Attachment(bd, inStream) {
}