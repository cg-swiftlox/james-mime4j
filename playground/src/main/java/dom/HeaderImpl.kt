package playground.dom

import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.dom.address.AddressList
import org.apache.james.mime4j.dom.address.Mailbox
import org.apache.james.mime4j.dom.address.MailboxList
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField
import org.apache.james.mime4j.dom.field.ContentTypeField
import org.apache.james.mime4j.message.HeaderImpl
import org.apache.james.mime4j.stream.Field

class HeaderImpl: HeaderImpl() {

    val lines: Int = -1

}