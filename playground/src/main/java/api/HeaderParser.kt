package api

import io.vertx.reactivex.core.buffer.Buffer
import org.apache.james.mime4j.codec.DecodeMonitor
import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder
import org.apache.james.mime4j.parser.MimeStreamParser
import org.apache.james.mime4j.stream.MimeConfig

class HeaderParser {
    companion object{

        fun parseHeader(buffer: Buffer, mailContentHandler: MailContentHandler): Header {
            println("HeaderParser: parseHeader()")

            val mime4jParserConfig = MimeConfig.Builder()
                    .setMaxLineLen(10000)
                    .setMaxHeaderLen(50000)
                    .setHeadlessParsing(null)
                    .build()
            val bodyDescriptorBuilder = DefaultBodyDescriptorBuilder()
            val mime4jParser = MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder)
            mime4jParser.isContentDecoding = true
            mime4jParser.setContentHandler(mailContentHandler)

            mime4jParser.parse(BufferInputStream(buffer))

            return mailContentHandler.header
        }

        fun parseHeader(buffer:Buffer): Header{
            return parseHeader(buffer, MailContentHandler())
        }
    }
}