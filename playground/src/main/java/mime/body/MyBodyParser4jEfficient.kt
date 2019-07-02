package playground.dom

import api.MailContentHandler
import api.MultipartState
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import org.apache.james.mime4j.dom.Entity
import org.apache.james.mime4j.dom.Multipart
import org.apache.james.mime4j.dom.field.ContentTypeField
import org.apache.james.mime4j.message.MessageImpl
import org.apache.james.mime4j.message.SingleBodyBuilder
import playground.Body
import playground.mime.body.FieldTypes
import playground.mime.body.MyHeaderParser
import playground.mime.body.dom.MyBodyPart
import playground.mime.body.dom.MyMultipartImpl
import java.nio.charset.Charset

/**
 * MyBodyParser4j parses an observable stream of body lines.
 * Parsed objects only contain line information (from, to) of the buffer.
 * @param mailContentHandler: Needed to retrieve the main header of the mail. (must be parsed already)
 */
class MyBodyParser4jEfficient(val mailContentHandler: MailContentHandler, nrLinesHeader: Int) {

    private var curState = MultipartState.START_MESSAGE

    private val message = MessageImpl()
    private lateinit var curEntity: Entity

    private var runningIndex = nrLinesHeader + 1

    private fun parse(stream: Observable<String>): Body {

        curEntity = message
        message.header = mailContentHandler.header

        val header = mailContentHandler.header
        val contentType = header.getField(FieldTypes.CONTENT_TYPE)

        when (contentType) {
            null -> parsePlainText(stream)
            is ContentTypeField -> {
                if(contentType.isMultipart) {
                    parseMultipart(stream)
                } else
                    parsePlainText(stream)
            }
        }

        return BodyImpl()
    }

    private val singleBodyStringBuilder = StringBuilder()
    private fun parsePlainText(stream: Observable<String>) {
//        myBody.body = "Test Body"

        stream.subscribe({line ->
            singleBodyStringBuilder.append(line)
            runningIndex++
        }, {
            throw it
        }, {
            if(curEntity.charset != null)
                curEntity.body = SingleBodyBuilder.create().setText(singleBodyStringBuilder.toString()).setCharset(Charset.forName(curEntity.charset)).build()
            else
                curEntity.body = SingleBodyBuilder.create().setText(singleBodyStringBuilder.toString()).build()
            println("single body processing complete")
        })

        stream.subscribe(::println)
    }

    private fun parseMultipart(stream: Observable<String>) {
        stream.subscribe({ line ->
            handleState(line)
            runningIndex++
        }, {
            throw it
        }, {
            println("multipart processing complete")
        })

    }

    private fun handleState(line: String) {
        when(curState) {
            MultipartState.START_MESSAGE -> startMessage(line)
            MultipartState.START_MULTIPART -> startMultipart(line)
            MultipartState.PREAMBLE -> preamble(line) //TODO("read start-body-part line")
            MultipartState.START_BODYPART -> startBodyPart(line)
            MultipartState.START_HEADER -> startHeader(line)
            MultipartState.FIELD -> parseField(line) // TODO("read field or set state to endHeader if no more field encountered")
            MultipartState.END_HEADER -> endHeader(line) // TODO("read empty line CRLF and decide which state is next (body / startMultipart)")
            MultipartState.BODY -> body(line) //TODO("read body until '-----=...' appears and set state to END_BODYPART")
            MultipartState.END_BODYPART -> endBodyPart(line) // TODO("read empty line and another line and decide which state is next (epilogue / startBodyPart)")
            MultipartState.EPILOGUE -> epilogue(line) // TODO("read epilogue or empty line")
            MultipartState.END_MULTIPART -> endMultipart(line) // TODO("read line with starting ID")
            MultipartState.END_MESSAGE -> endMessage(line)
        }
    }

    private fun startMessage(line: String) {
        // create message instance
        // -> already done in parse method
        // -> message header already parsed as well
        // -> START_MULTIPART

        curState = MultipartState.START_MULTIPART
        handleState(line)
    }

    private fun startMultipart(line: String) {
        // according to curEntity.header -> create suiting Multipart Type and assign to curEntity.body
        // assign multipart to curBody
        // -> PREAMBLE

        val contentType = curEntity.header.getField(FieldTypes.CONTENT_TYPE)
        if (contentType is ContentTypeField) {
            curEntity.body = MyMultipartImpl(contentType.subType)
        } else {
            throw Exception("No content-type header found")
        }
        curState = MultipartState.PREAMBLE
        if(curEntity.body is MyMultipartImpl) {
            val multipart = curEntity.body as MyMultipartImpl
            multipart.preambleFrom = runningIndex
        }
        handleState(line)
    }

    private var preambleStringBuilder = StringBuilder()
    private fun preamble(line: String) {
        // read lines until --
        // then State = START_BODYPART

        if (line.startsWith("--")) {
            if(curEntity.body is MyMultipartImpl) {
                val multipart = curEntity.body as MyMultipartImpl
                multipart.preambleTo = runningIndex - 1
            }
            preambleStringBuilder.clear()

            curState = MultipartState.START_BODYPART
            handleState(line)
        } else {
            preambleStringBuilder.append(line)
        }
    }

    private fun startBodyPart(line: String){
        // maybe store boundaryID
        // create BodyPart entity and add to bodyParts of curBody + bodyPart.parent = curEntity
        // set curEntity = bodypart entity
        // -> START_HEADER

        val bodyPart = MyBodyPart()
        bodyPart.parent = curEntity
        bodyPart.from = runningIndex

        val multipart = curEntity.body as Multipart // in state START_BODY, curEntity.body must be multipart
        multipart.addBodyPart(bodyPart)
        curEntity = bodyPart

        curState = MultipartState.START_HEADER
    }

    private var headerParser = MyHeaderParser()
    private val headerStringBuilder = StringBuilder()
    private fun startHeader(line: String) {
        // create / clear StringBuilder for header lines
        // -> state = FIELD

        headerStringBuilder.clear()
        curState = MultipartState.FIELD
        handleState(line)
    }

    private fun parseField(line:String) {
        // append line to StringBuilder
        // read lines until \r\n or \n
        // -> END_HEADER

        if(isEmptyLine(line)) {
            curState = MultipartState.END_HEADER
            handleState(line)
        } else {
            headerStringBuilder.append(line)
        }
    }

    private fun isEmptyLine(line: String): Boolean {
        return line == "\r\n" || line == "\n"
    }

    private fun endHeader(line: String) {
        // parse header
        // set header to curEntity.header
        // according to header information, decide what state is next
        // - START_MULTIPART -> if header is multipart (maybe create MultipartImpl and assign to message.body) or do it in "startMultipart"
        // - BODY -> else (maybe create TextBody type and assign to message.body)

        val header = headerParser.parseHeader(headerStringBuilder.toString())
        curEntity.header = header

        if(curEntity.isMultipart) {
            curState = MultipartState.START_MULTIPART
        } else {
            curState = MultipartState.BODY
        }
    }

    private val bodyStringBuilder = StringBuilder()
    private fun body(line: String) {
        // read lines until line starts with "--"
        // store lines in StringBuilder
        // -> END_BODYPART (deliver line)

        if (line.startsWith("--")) {
            curState = MultipartState.END_BODYPART
            handleState(line)
        } else {
            bodyStringBuilder.append(line)
        }
    }

    private fun endBodyPart(line: String) {
        // store StringBuilder buffer in curEntity.body
        // set curEntity = curEntity.parent
        // - if line ends with "--" -> EPILOGUE
        // - else START_BODYPART

        if(curEntity.isMultipart) {
            // do nothing -> already assigned
            // TODO: can curEntity be a multipart in this state???
        } else {
            if(curEntity.charset != null)
                curEntity.body = SingleBodyBuilder.create().setText(bodyStringBuilder.toString()).setCharset(Charset.forName(curEntity.charset)).build()
            else
                curEntity.body = SingleBodyBuilder.create().setText(bodyStringBuilder.toString()).build()
            bodyStringBuilder.clear()
        }
        // store the line information "from" in the current bodyPart, before we close it
        if(curEntity is MyBodyPart) {
            val myBodyPart = curEntity as MyBodyPart
            myBodyPart.to = runningIndex-1
        }
        // BodyPart ends here -> therefore this level is done and we need to set the parent as the current level
        curEntity = curEntity.parent
        // if the line ends with "--", then the next state is an EPILOGUE
        // otherwise the next state is a START_BODYPART
        if(line.trimEnd('\r', '\n').endsWith("--")) { // TODO: maybe do a better check -> check if boundaryID is ident
            curState = MultipartState.EPILOGUE
            if(curEntity.body is MyMultipartImpl) {
                val multipart = curEntity.body as MyMultipartImpl
                multipart.epilogueFrom = runningIndex + 1
            }
        } else {
            curState = MultipartState.START_BODYPART
            handleState(line)
        }
    }

    private fun epilogue(line: String) {
        // read line and store in curEntity.body.epilogue
        // read until email ends or "--"
        // - if "--" then END_MULTIPART (deliver line)
        // - else -> Observable complete

        if(line.startsWith("--")) {
            curState = MultipartState.END_MULTIPART
            handleState(line)
        } else {
            if(curEntity.body is MyMultipartImpl) {
                val multipart = curEntity.body as MyMultipartImpl
                multipart.epilogueTo = runningIndex
            }
        }
    }

    private fun endMultipart(line: String) {
        if(line.startsWith("--")) {
            curState = MultipartState.END_BODYPART
            handleState(line)
        }
    }

    private fun endMessage(line: String) {
        // should not be reached
    }

    companion object {
        fun parse(stream: Observable<String>, mailContentHandler: MailContentHandler, nrLinesHeader: Int): Single<Body> {
            println("companion object parse")

            return Single.create(SingleOnSubscribe<Body> { emitter ->
                println("single create")
                val myBodyParser = MyBodyParser4jEfficient(mailContentHandler, nrLinesHeader)
                emitter.onSuccess(myBodyParser.parse(stream))
            })
        }
    }
}