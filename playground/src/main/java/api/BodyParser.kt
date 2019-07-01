package api

import io.vertx.reactivex.core.buffer.Buffer
import org.apache.james.mime4j.codec.DecodeMonitor
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder
import org.apache.james.mime4j.parser.MimeStreamParser
import org.apache.james.mime4j.stream.MimeConfig
import playground.Body

class BodyParser {

    companion object {
        fun parseBody(buffer: Buffer, mailContentHandler: MailContentHandler): Body {
            println("BodyParser: onComplete()")

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

            return mailContentHandler.body
        }

        fun parseBody(buffer: Buffer): Body {
            return parseBody(buffer, MailContentHandler())
        }
    }
}