package playground.mime.body

import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.message.HeaderImpl

class MyBodyPart(val parent: MyMultiPart) {
    var subHeader: Header = HeaderImpl()

    var level = 0
    var body: String? = null
    var multiPart: MyMultiPart? = null
}