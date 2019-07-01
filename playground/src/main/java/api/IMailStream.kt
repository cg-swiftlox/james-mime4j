package api

import io.reactivex.Observable
import io.reactivex.Single
import org.apache.james.mime4j.dom.Header
import playground.Attachment
import playground.dom.IContent

interface IMailStream {

    fun getHeader(): Observable<String> // needed for storing in database
    fun getParsedHeader(): Single<Header> // needed for decision making (what happens with the mail ... store, forward, abort)
    fun deleteHeader(): Unit

    fun getContent(): Observable<String> // needed for storing in database
    fun getParsedContent(): Single<IContent> // needed for decision making (what happens with the mail ... store, forward, abort)
    fun deleteContent(): Unit

    fun getAttachments(): Observable<String> // needed for storing in database
    fun getParsedAttachments(): Observable<Attachment> // needed for decision making (what happens with the mail ... store, forward, abort)
    fun deleteAttachments(): Unit
}