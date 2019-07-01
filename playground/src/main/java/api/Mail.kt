package api

import io.reactivex.Observable
import io.reactivex.Single
import org.apache.james.mime4j.dom.Header

class Mail(val stream: Observable<String>) {

    private val mailSplitter: MailSplitter = MailSplitter(stream)
    var parser: MailParser = MailParser()

    init {
        mailSplitter.subscribe()
        println()
        println()
        mailSplitter.subscribe()
        mailSplitter.getHeaderStream().subscribe { println("      HeaderLine: $it") }
    }

    fun getHeader(): Single<Header> {
        return parser.getHeader()
    }

    fun parseBody() {
        parser.getBody().subscribe { body ->
            println("body")
            println(body)
        }
    }
}