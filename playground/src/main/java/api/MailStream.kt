package api

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import io.vertx.reactivex.core.buffer.Buffer
import org.apache.james.mime4j.dom.Header
import playground.Attachment
import playground.dom.IContent
import playground.dom.MyBodyParser4j
import java.util.*

class MailStream(val stream: Observable<String>): IMailStream {

    val buffer: LinkedList<String> = LinkedList()
    private var headerTo: Int = 0
    private var bodyTo: Int = 0

    val contentHandler: MailContentHandler = MailContentHandler()
    val splitter = MailSplitter(stream)

    var headerSingle: Single<Header> = SingleSubject.create()

    init {
        subscribeToHeaderStream()
        subscribeToBodyStream()
        splitter.subscribe()
    }

    private fun subscribeToHeaderStream() {
        splitter.getHeaderStream().subscribe({ line ->
            buffer.add(line)
        },{
            throw it
        },{
            headerTo = buffer.size - 1

            val headerSublist = buffer.subList(0, headerTo)
            val headerSublistObservable = Observable.fromIterable(headerSublist)

            // parse header - blocking
            val header = HeaderParser.parseHeader(listToBuffer(buffer.subList(0, headerTo)), contentHandler)
            headerSingle = Single.just(header)
        })

    }

    private fun subscribeToBodyStream() {
        splitter.getBodyStream().subscribe({ line ->
            buffer.add(line)
        }, {
            throw it
        }, {
            bodyTo = buffer.size

            // parse body
            // TODO: maybe don't parse body from start ... only parse when needed
//            val body = MyBodyParser.parse(Observable.fromIterable(buffer.subList(headerTo, bodyTo)), contentHandler, headerTo).subscribe()
            val body = MyBodyParser4j.parse(Observable.fromIterable(buffer.subList(headerTo, bodyTo)), contentHandler, headerTo).subscribe()


        })
    }

    override fun getHeader(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParsedHeader(): Single<Header> {
        return headerSingle
    }

    override fun deleteHeader() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContent(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParsedContent(): Single<IContent> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteContent() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAttachments(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParsedAttachments(): Observable<Attachment> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAttachments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun listToBuffer(list: MutableList<String>): Buffer {
        return list.fold(Buffer.buffer(), { buffer, line ->
            buffer.appendString(line)
        })
    }
}