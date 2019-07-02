package api

import io.reactivex.Observable
import io.reactivex.Single
import org.apache.james.mime4j.dom.Entity
import org.apache.james.mime4j.dom.Header

interface IMailStream {

    fun getHeader(): Observable<String> // needed for storing in database
    fun getParsedHeader(): Single<Header> // needed for decision making (what happens with the mail ... store, forward, abort)
    fun deleteHeader(): Unit

    fun getBody(): Observable<String>
    fun deleteBody(): Observable<String>

    fun getPlainText(): Observable<String>
    fun getParsedPlainText(): Single<Entity>
    fun deletePlainText(): Unit

    fun getHTML(): Observable<String>
    fun getParsedHTML(): Single<Entity>
    fun deleteHTML(): Unit

//    fun getContent(): Observable<String> // needed for storing in database
//    fun getParsedContent(): Single<IContent> // needed for decision making (what happens with the mail ... store, forward, abort)
//    fun deleteContent(): Unit

    fun getAttachments(): Observable<String> // needed for storing in database
    fun getParsedAttachments(): Observable<Entity> // needed for decision making (what happens with the mail ... store, forward, abort)
    fun deleteAttachments(): Unit
}