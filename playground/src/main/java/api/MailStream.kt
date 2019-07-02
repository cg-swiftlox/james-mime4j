package api

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.UnicastProcessor
import io.reactivex.subjects.SingleSubject
import io.reactivex.subjects.UnicastSubject
import io.vertx.reactivex.core.buffer.Buffer
import org.apache.james.mime4j.dom.Entity
import org.apache.james.mime4j.dom.Header
import playground.Attachment
import playground.dom.IContent
import playground.dom.MyBodyParser4j
import playground.dom.MyBodyParser4jEfficient
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 */
class MailStream(val stream: Observable<String>): IMailStream {

    val buffer: LinkedList<String> = LinkedList()
    private var headerTo: Int = 0
    private var bodyTo: Int = 0

    val contentHandler: MailContentHandler = MailContentHandler()
    val splitter = MailSplitter(stream)

    var headerProcessor: BehaviorProcessor<Header> = BehaviorProcessor.create()

//    init {
//        subscribeToHeaderStream()
//        subscribeToBodyStream()
//        splitter.subscribe()
//    }

    fun parse() {
        subscribeToHeaderStream()
        subscribeToBodyStream()
        splitter.subscribe()
    }

    private fun subscribeToHeaderStream() {
        splitter.getHeaderStream().subscribe({ line ->
            println("PARSING HEADER LINES")
            buffer.add(line)
        },{
            throw it
        },{
            println("PARSING HEADER LINES DONE")
            headerTo = buffer.size - 1

            val headerSublist = buffer.subList(0, headerTo)
            val headerSublistObservable = Observable.fromIterable(headerSublist)

//            parse header - blocking
//            val header = HeaderParser.parseHeader(listToBuffer(buffer.subList(0, headerTo)), contentHandler)

            val header = HeaderParser.parseHeader(listToBuffer(splitter.getBuffer().subList(0, headerTo)), contentHandler)
//            headerSingle.onNext(header)
            headerProcessor.onNext(header)
        })

//        getParsedHeader().subscribe { header ->
//            println("WWWWWWWWWW: parsedHeader subscribe")
//            println("WWWWWWWWWW: content-type: " + header.getField("content-type"))
//        }
    }

    private fun subscribeToBodyStream() {
        splitter.getBodyStream().subscribe({ line ->
            println("PARSING BODY LINES")
            buffer.add(line)
        }, {
            throw it
        }, {
            println("PARSING BODY LINES DONE")
            bodyTo = buffer.size

            // parse body
            // TODO: maybe don't parse body from start ... only parse when needed
//            val body = MyBodyParser.parse(Observable.fromIterable(buffer.subList(headerTo, bodyTo)), contentHandler, headerTo).subscribe()
//            val body = MyBodyParser4j.parse(Observable.fromIterable(buffer.subList(headerTo, bodyTo)), contentHandler, headerTo).subscribe()
//            val body = MyBodyParser4j.parse(splitter.getBodyStream(), contentHandler, headerTo).subscribe()
            val body = MyBodyParser4jEfficient.parse(splitter.getBodyStream(), contentHandler, headerTo).subscribe()


        })
    }

    override fun getHeader(): Observable<String> {
        return splitter.getHeaderStream()
    }

    override fun getParsedHeader(): Single<Header> {
        return headerProcessor.firstOrError()
    }

    override fun deleteHeader() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAttachments(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParsedAttachments(): Observable<Entity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAttachments() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getBody(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteBody(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPlainText(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParsedPlainText(): Single<Entity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deletePlainText() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getHTML(): Observable<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParsedHTML(): Single<Entity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteHTML() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun listToBuffer(list: MutableList<String>): Buffer {
        return list.fold(Buffer.buffer(), { buffer, line ->
            buffer.appendString(line)
        })
    }
}