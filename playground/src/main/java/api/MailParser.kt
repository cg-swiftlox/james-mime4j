package api

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import io.vertx.reactivex.core.buffer.Buffer
import org.apache.james.mime4j.dom.Header

class MailParser {
    val parsedHeader = SingleSubject.create<Header>()
    private val mailContentHandler = MailContentHandler()

    fun parse(stream: Observable<String>){
        parse(MailSplitter(stream))
    }

    /**
     * Starts parsing from a mail from the MailStream
     * - Only header gets parsed
     * - MyBody is parsed on demand
     */
    fun parse(mailSplitter: MailSplitter) {
        // declare header
        val headerParser: Single<Header> = declareHeaderParser(mailSplitter.getHeaderStream())
        // parse header
//        headerParser.subscribe(parsedHeader)

        // declare body
//        val bodyParser: Single<MyBody> = declareBodyParser(mailStream.getFullStream())
        // don't parse header! only store declaration and parse when requested
//        val bodyParser: Single<Body> = declareMyBodyParser(mailSplitter)
////        parsedBody = bodyParser
//        headerParser.subscribe { header ->
//            parsedHeader.onSuccess(header)
//            bodyParser.subscribe(parsedBody)
//        }

        // start reading from stream
        mailSplitter.subscribe()
    }

    private fun declareHeaderParser(headerStream: Observable<String>): Single<Header> {
        return headerStream.reduceWith({Buffer.buffer()}){ buffer, nextElement ->
            println("MailParser: collect header line")
            buffer.appendString(nextElement)
        }.map { buffer: Buffer ->
            HeaderParser.parseHeader(buffer, mailContentHandler)
        }
    }

    private fun declareBodyParser(bodyStream: Observable<String>): Single<Unit> {
        return bodyStream.reduceWith({Buffer.buffer()}, {buffer, nextElement ->
            println("MailParser: collect body line")
            buffer.appendString(nextElement)
        }).map { buffer: Buffer ->
            BodyParser.parseBody(buffer, mailContentHandler)
        }
    }

//    private fun declareMyBodyParser(mailSplitter: MailSplitter): Single<Body> {
//        return MyBodyParser.parse(mailSplitter.getBodyStream(), mailContentHandler, 0) //mailSplitter.getNrLinesHeader())
//    }

    fun getHeader(): Single<Header> {
        return parsedHeader
    }

//    companion object {
//        fun parseMail(stream: Observable<String>): MailParser {
//            val parser = MailParser()
//            parser.parse(MailStream(stream))
//            return parser
//        }
//
//        fun parseMail(mailStream: MailStream): MailParser {
//            val parser = MailParser()
//            parser.parse(mailStream)
//            return parser
//        }
//    }
}