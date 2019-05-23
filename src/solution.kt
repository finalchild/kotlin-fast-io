import java.io.FilterOutputStream
import java.io.OutputStream

val fastOutStream = run {
    var stream = System.out as OutputStream
    while (stream is FilterOutputStream) {
        val outField = FilterOutputStream::class.java.getDeclaredField("out")
        outField.isAccessible = true
        stream = outField.get(stream) as OutputStream
    }
    stream
}
val fastOutBuf = ByteArray(8192)
var fastOutBufCount = 0

fun print(s: ByteArray) {
    if (s.size >= fastOutBuf.size) {
        flush()
        fastOutStream.write(s, 0, s.size)
        return
    }
    if (s.size > fastOutBuf.size - fastOutBufCount) {
        flush()
    }
    System.arraycopy(s, 0, fastOutBuf, fastOutBufCount, s.size)
    fastOutBufCount += s.size
}

fun flush() {
    if (fastOutBufCount > 0) {
        fastOutStream.write(fastOutBuf, 0, fastOutBufCount)
        fastOutBufCount = 0
    }
}

fun print(o: Any?) {
    print(o.toString())
}

fun print(s: String) {
    print(s.toByteArray())
}

fun println(o: Any?) {
    print(o.toString())
    print("\n")
}
