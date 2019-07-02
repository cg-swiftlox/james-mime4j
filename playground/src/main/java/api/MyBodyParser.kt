package api

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import playground.Body
import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.dom.field.ContentTypeField
import playground.dom.BodyImpl
import playground.mime.body.*
import playground.mime.body.dom.MyBodyPart
import kotlin.text.StringBuilder

class MyBodyParser(val mailContentHandler: MailContentHandler, val nrLinesHeader: Int) {

    private var currentState = MultipartState.START_MULTIPART

    private val myBody = MyBody()
    private var body: String? = null
    private var currentMultiPart: MyMultiPart? = null
    private var currentBodyPart: MyBodyPart? = null

    private fun parse(stream: Observable<String>): Body {

        val header = mailContentHandler.header
        val contentType = header.getField(FieldTypes.CONTENT_TYPE)

        when (contentType) {
            null -> parsePlainText(stream)
            is ContentTypeField -> {
                if(contentType.isMultipart)
                    parseMultipart(stream)
                else
                    parsePlainText(stream)
            }
        }

        return BodyImpl()
    }

    private fun parsePlainText(stream: Observable<String>) {
        myBody.body = "Test Body"
        stream.subscribe(::println)
    }

    private fun parseMultipart(stream: Observable<String>) {
        myBody.multiPart = MyMultiPart()
        stream.subscribe({ line ->
            when(currentState) {
                MultipartState.START_MULTIPART -> startMultipart(line)
                MultipartState.PREAMBLE -> preamble(line) //TODO("read start-body-part line")
                MultipartState.START_BODYPART -> startBodyPart(line)
                MultipartState.START_HEADER -> startHeader(line)
                MultipartState.FIELD -> parseField(line) // TODO("read field or set state to endHeader if no more field encountered")
//                MultipartState.END_HEADER -> endHeader() // TODO("read empty line CRLF and decide which state is next (body / startMultipart)")
                MultipartState.BODY -> body(line) //TODO("read body until '-----=...' appears and set state to END_BODYPART")
                MultipartState.END_BODYPART -> endBodyPart(line) // TODO("read empty line and another line and decide which state is next (epilogue / startBodyPart)")
//                MultipartState.EPILOGUE -> TODO("read epilogue or empty line")
                MultipartState.END_MULTIPART -> TODO("read line with starting ID")
            }
        }, {
            throw it
        }, {
            println("multipart processing complete")
        })

    }

    private fun startMultipart(line: String) {
        if(currentMultiPart == null) {
            currentMultiPart = myBody.multiPart
            currentMultiPart!!.level = 1
        } else {
            val level = currentMultiPart!!.level
            currentMultiPart = MyMultiPart(currentBodyPart!!)
            currentMultiPart!!.level = level+1
        }
        println("${currentMultiPart!!.level}")
        currentState = MultipartState.PREAMBLE
    }

    private fun preamble(line: String) {
        println("reading preample")
        if(line.startsWith("--")){
            println("boundary found: $line")
            currentMultiPart!!.boundaryLine = line
            currentMultiPart!!.boundaryID = line.substring(2).trimEnd('\r', '\n')
            currentState = MultipartState.START_BODYPART
        }
    }

    private fun startBodyPart(line: String){
//        currentBodyPart = MyBodyPart(currentMultiPart!!)
        currentMultiPart!!.addBodyPart(currentBodyPart!!)
        println("${currentMultiPart!!.level}.${currentMultiPart!!.countBodyParts()}")

        currentState = MultipartState.START_HEADER
        startHeader(line)
    }

    private var parser = MyHeaderParser()
    private var subHeaderStringBuilder: StringBuilder = StringBuilder()
    private fun startHeader(line: String) {
        parser = MyHeaderParser()
        subHeaderStringBuilder.clear()
        subHeaderStringBuilder.append(line)
        currentState = MultipartState.FIELD
    }

    private fun parseField(line:String) {
        if(isEmptyLine(line)){
            val subHeader = parser.parseHeader(subHeaderStringBuilder.toString())
            endHeader(subHeader)
        } else {
            subHeaderStringBuilder.append(line)
        }
    }

    private fun isEmptyLine(line: String): Boolean {
        return line == "\r\n" || line == "\n"
    }

    private fun endHeader(subHeader: Header) {
//        currentBodyPart!!.subHeader = subHeader

        val contentType = subHeader.getField(FieldTypes.CONTENT_TYPE)
        when (contentType) {
            null -> currentState = MultipartState.BODY
            is ContentTypeField -> {
                if(contentType.isMultipart)
                    currentState = MultipartState.START_MULTIPART
                else
                    currentState = MultipartState.BODY
            }
        }
    }

    private var bodyString: String = String()
    private fun body(line: String) {
        if(line.startsWith("--")) {
            currentState = MultipartState.END_BODYPART
            endBodyPart(line)
        } else
            bodyString += line
    }

    private fun endBodyPart(line: String) {
        val partID = line.substring(2).trimEnd('\r', '\n')
        if(partID == currentMultiPart!!.boundaryID) {
            // -> check when boundaryID of multipart is assigned -> should be assigned at header parsing and not when first occurence

            // -> startBodyPart
            currentState = MultipartState.START_BODYPART

        } else if (partID == "${currentMultiPart!!.boundaryID}--") {
            // -> epilogue -> endMultipart
            currentState = MultipartState.END_MULTIPART
//            currentMultiPart = currentBodyPart!!.parent
        }
    }

    private fun endMultipart(line: String) {

    }

    companion object {
        fun parse(stream: Observable<String>, mailContentHandler: MailContentHandler, nrLinesHeader: Int): Single<Body> {
            println("companion object parse")

            return Single.create(SingleOnSubscribe<Body> { emitter ->
                println("single create")
                val myBodyParser = MyBodyParser(mailContentHandler, nrLinesHeader)
                emitter.onSuccess(myBodyParser.parse(stream))
            })
        }
    }
}

enum class MultipartState {
    START_MESSAGE,
    START_HEADER,
    FIELD,
    END_HEADER,
    BODY,
    START_MULTIPART,
    PREAMBLE,
    START_BODYPART,
    END_BODYPART,
    EPILOGUE,
    END_MULTIPART,
    END_MESSAGE
}

enum class FieldState {
    FIELD_IN_PROCESS,
    FIELD_DONE
}