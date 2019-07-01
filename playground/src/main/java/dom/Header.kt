package playground.dom

import org.apache.james.mime4j.dom.address.AddressList
import org.apache.james.mime4j.dom.address.Mailbox
import org.apache.james.mime4j.dom.address.MailboxList
import org.apache.james.mime4j.dom.field.ContentTransferEncodingField
import org.apache.james.mime4j.dom.field.ContentTypeField
import org.apache.james.mime4j.stream.Field

interface Header {
    fun getSubject(): String
    fun getFromList(): MailboxList
    fun getToList(): AddressList
    fun getCcList(): AddressList
    fun getReplyToList(): AddressList
    fun getSender(): Mailbox

    fun getFieldList(): List<Field>
    fun getContentTypeField(): ContentTypeField?
    fun getContentTransferEncodingField(): ContentTransferEncodingField?
}