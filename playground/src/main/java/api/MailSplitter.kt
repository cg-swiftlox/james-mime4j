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
 *
 *  Data structure class for mails.
 */
class MailSplitter(val stream: Observable<String>) {

    private var currentState: MailState = MailState.HEADER_START
    private var headerTo = 0
    private var bodyTo = 0

    private val headerProcessor = BehaviorProcessor.create<String>()
    private val bodyProcessor = BehaviorProcessor.create<String>()

    private val buffer: LinkedList<String> = LinkedList()

    fun subscribe(): Disposable {
        return stream.subscribe({ line ->
            println("MailStream received line ...")
            buffer.add(line)
            when (currentState) {
                MailState.HEADER_START -> {
                    headerProcessor.onNext(line)
                    if(line == System.lineSeparator() || line.isBlank()) {
                        currentState = MailState.BODY_START
                        println("headerProcessor.onComplete()")
                        headerProcessor.onComplete()
                        headerTo = buffer.size-1
                        println("HeaderTo Index: $headerTo")
                    }
                }
                MailState.BODY_START ->  {
                    bodyProcessor.onNext(line)
                }
            }
        }, {
            throw it
        }, {
            println("MailStream: onComplete()")
            currentState = MailState.BODY_END

            bodyProcessor.onComplete()
            bodyTo = buffer.size-1
            println("BodyTo Index: $bodyTo")
        })
    }

    fun getBuffer(): LinkedList<String> {
        return buffer
    }

    /**
     * Returns the stream of the header.
     * Header might not have been fully loaded.
     * Lines will be added to the stream when they arrive.
     */
    fun getHeaderStream(): Observable<String> {
        return Observable.defer {
            val subList = if(headerTo == 0) buffer else buffer.subList(0, headerTo)
            println("----------------------HeaderStream---------------------------")
            println("headerTo: $headerTo | bufferSize: ${buffer.size}")
            println(subList)
            println("-------------------------------------------------")
            Observable.fromIterable(subList).concatWith(headerProcessor.toObservable())
        }
//        return headerProcessor.toObservable()
    }

    /**
     * Returns the stream of the body.
     * Body might not have been fully loaded.
     * Lines will be added to the stream when they arrive.
     *
     * HeaderStream is already loaded.
     */
    fun getBodyStream(): Observable<String> {
        return Observable.defer {
            val subList = if (headerTo == 0) emptyList<String>() else buffer.subList(headerTo, buffer.size-1)
            println("-----------------------BodyStream--------------------------")
            println("headerTo: $headerTo | bufferSize: ${buffer.size}")
            println(subList)
            println("-------------------------------------------------")
            Observable.fromIterable(subList).concatWith(bodyProcessor.toObservable())
        }
//        return bodyProcessor.toObservable()
    }
}

enum class MailState {
    HEADER_START, BODY_START, BODY_END
}