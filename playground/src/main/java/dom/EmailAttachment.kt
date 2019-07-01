package playground.dom

import org.apache.james.mime4j.stream.BodyDescriptor
import playground.Attachment
import java.io.InputStream

class EmailAttachment(bd: BodyDescriptor?, inStream: InputStream?): Attachment(bd, inStream) {

}