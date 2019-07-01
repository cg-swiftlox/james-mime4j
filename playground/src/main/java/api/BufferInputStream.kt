package api

import io.vertx.reactivex.core.buffer.Buffer
import java.io.InputStream

class BufferInputStream(private val buffer: Buffer) : InputStream() {

    var pos = 0
    override fun read(): Int {
//        println("${System.currentTimeMillis()}: Byte read from stream ...")
        if (pos >= buffer.length()) return -1
        else return buffer.getByte(pos++).toInt() and 0xff
    }
}