package playground

import api.BufferInputStream
import api.MailContentHandler
import api.MailStream
import io.reactivex.Emitter
import io.reactivex.Observable
import io.vertx.reactivex.core.buffer.Buffer
import org.apache.james.mime4j.codec.DecodeMonitor
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder
import org.apache.james.mime4j.parser.MimeStreamParser
import org.apache.james.mime4j.stream.MimeConfig
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset

fun main(){
    val fileObservable = readFromFile()
    val mailStream = MailStream(fileObservable)

    mailStream.getParsedHeader().subscribe { header ->
        println("parsedHeader subscribe")
        println("content-type: " + header.getField("content-type"))
    }

}

fun readFromFile(): Observable<String> {
    val reader: () -> BufferedReader = {
        BufferedReader(FileReader("/home/christoph/Downloads/samples/messages/m1005.txt"))
    }
    val emitter: (BufferedReader, Emitter<String>) -> Unit = { reader, emitter ->
        val line = reader.readLine()
        println("FileReader read line ...")
        if (line != null)
            emitter.onNext(line + System.lineSeparator()) // "\r\n")
        else
            emitter.onComplete()
    }
    val closer: (BufferedReader) -> Unit = { reader -> reader.close() }
    return Observable.generate(reader, emitter, closer)
}



fun play() {
    val myObservable = Observable.just("1", "2", "3")

    myObservable.subscribe { println("1: $it") }
    myObservable.subscribe { println("2: $it")}
}

fun readFromBuffer() {
    val myBuffer= Buffer.buffer(File("/home/christoph/Downloads/samples/messages/m1005.txt").readText(Charset.defaultCharset()))

    val mime4jParserConfig = MimeConfig.Builder()
            .setMaxLineLen(10000)
            .setMaxHeaderLen(50000)
            .setHeadlessParsing(null)
            .build()
    mime4jParserConfig.maxContentLen
    val bodyDescriptorBuilder = DefaultBodyDescriptorBuilder()
    val mime4jParser = MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder)
    mime4jParser.isContentDecoding = true
    mime4jParser.setContentHandler(MailContentHandler())

    mime4jParser.parse(BufferInputStream(myBuffer))
//    mime4jParser.parseHeader(BufferInputStream(myBuffer))

}