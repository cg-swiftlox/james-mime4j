package playground.mime.body

class MyMultiPart(val parent: MyBodyPart? = null){

    var level = 0
    var boundaryLine: String? = null
    var boundaryID: String? = null
    private val bodyParts: ArrayList<MyBodyPart> = ArrayList()

    fun addBodyPart(bodyPart: MyBodyPart) {
        bodyParts.add(bodyPart)
    }

    fun getCurrentBodyPart(): MyBodyPart {
        return bodyParts[bodyParts.size-1]
    }

    fun countBodyParts(): Int = bodyParts.size
}