package api

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.UnicastProcessor
import io.reactivex.subjects.SingleSubject
import java.util.*

/**
 * Separates a mail stream of lines into two streams:
 *  - HeaderStream: Stream of lines containing only header information
 *  - BodyStream: Stream of lines containing only body information
 *  - FullStream: Stream of complete mail
 */
class MailSplitter(val stream: Observable<String>) {

    private var currentState: MailState = MailState.HEADER_START

    private val headerProcessor = BehaviorProcessor.create<String>()
    private val bodyProcessor = UnicastProcessor.create<String>()
    private val fullProcessor = UnicastProcessor.create<String>()

    private var headerNrLines: Single<Long> = SingleSubject.create<Long>()
    private var bodyNrLines: Single<Long> = SingleSubject.create<Long>()
    private var fullNrLines: Single<Long> = SingleSubject.create<Long>()

    private val buffer: LinkedList<String> = LinkedList()

    fun subscribeWithObservers(headerObserver: (line: String) -> Unit, bodyObservable: (line: String) -> Unit) {
        headerProcessor.subscribe(headerObserver)
        bodyProcessor.subscribe(bodyProcessor)
        subscribe()
    }

    fun subscribe(): Disposable {
        return stream.subscribe({ line ->
            println("MailStream received line ...")
            buffer.add(line)
            when (currentState) {
                MailState.HEADER_START -> {
                    headerProcessor.onNext(line)
                    fullProcessor.onNext(line)
                    if(line == System.lineSeparator() || line.isBlank()) {
                        currentState = MailState.BODY_START
                        println("headerProcessor.onComplete()")
                        headerProcessor.onComplete()
                        headerNrLines = headerProcessor.count()
                    }
                }
                MailState.BODY_START ->  {
                    bodyProcessor.onNext(line)
                    fullProcessor.onNext(line)
                }
            }
        }, {
            throw it
        }, {
            println("MailStream: onComplete()")
            currentState = MailState.BODY_END

            bodyProcessor.onComplete()
            bodyNrLines = bodyProcessor.count()
            fullProcessor.onComplete()
            fullNrLines = fullProcessor.count()
        })
    }

    fun getBuffer(): LinkedList<String> {
        return buffer
    }

    fun getHeaderStream(): Observable<String> {
        return headerProcessor.toObservable()
    }

    fun getBodyStream(): Observable<String> {
        return bodyProcessor.toObservable()
    }

    fun getFullStream(): Observable<String> {
        return fullProcessor.toObservable()
    }

    fun getNrLinesHeader(): Single<Long> {
        return headerNrLines
    }

    fun getNrLinesBody(): Single<Long> {
        return bodyNrLines
    }

    fun getNrLinesFull(): Single<Long> {
        return fullNrLines
    }
}

enum class MailState {
    HEADER_START, BODY_START, BODY_END
}