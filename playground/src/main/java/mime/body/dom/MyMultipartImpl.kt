package playground.mime.body.dom

import org.apache.james.mime4j.message.MultipartImpl

/**
 * Implementation of Multipart without storing the lines locally.
 * Only line numbers (from, to) are stored.
 */
class MyMultipartImpl(subType: String): MultipartImpl(subType) {
    var hasPreamble: Boolean = false
    var preambleFrom: Int = 0
        set(value) {
            hasPreamble = true
            field = value
        }
    var preambleTo: Int = 0
        set(value) {
            hasPreamble = true
            field = value
        }

    var hasEpilogue: Boolean = false
    var epilogueFrom: Int = 0
        set(value) {
            hasEpilogue = true
            field = value
        }
    var epilogueTo: Int = 0
        set(value) {
            hasEpilogue = true
            field = value
        }
}