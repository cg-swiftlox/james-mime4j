package playground

import org.apache.james.mime4j.stream.BodyDescriptor
import java.io.InputStream

abstract class Attachment(bd: BodyDescriptor?, inStream: InputStream?) {

    var from: Int = -1
    var to: Int = -1

}