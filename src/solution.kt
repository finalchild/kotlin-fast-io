import java.io.FilterOutputStream
import java.io.OutputStream
import java.io.FilterInputStream
import java.io.InputStream
import kotlin.experimental.and

val fastOutStream = run {
    val outField = FilterOutputStream::class.java.getDeclaredField("out")!!
    outField.isAccessible = true
    var stream = System.out as OutputStream
    while (stream is FilterOutputStream) {
        stream = outField.get(stream) as OutputStream
    }
    stream
}
val fastOutBuf = ByteArray(8192)
var fastOutBufCount = 0

fun fastOutWrite(s: ByteArray) {
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
    fastOutWrite(s.toByteArray())
}

fun println(o: Any?) {
    print(o.toString())
    print("\n")
}

val fastInStream = run {
    val inField = FilterInputStream::class.java.getDeclaredField("in")!!
    inField.isAccessible = true
    var stream = System.`in` as InputStream
    while (stream is FilterInputStream) {
        stream = inField.get(stream) as InputStream
    }
    stream
}
val fastInBuf = ByteArray(8192)
var fastInBufPos = 0
var fastInBufCount = 0

fun fastInFill() {
    fastInBufPos = 0
    fastInBufCount = 0
    val n = fastInStream.read(fastInBuf, fastInBufPos, fastInBuf.size - fastInBufPos)
    if (n > 0)
        fastInBufCount = n
}

fun fastInRead(): Byte {
    if (fastInBufPos >= fastInBufCount) {
        fastInFill()
        if (fastInBufPos >= fastInBufCount)
            return -1
    }
    val result = (fastInBuf[fastInBufPos] and 0xff.toByte())
    fastInBufPos++
    return result
}

fun scanInt(): Int {
     var c: Byte
    do {
        c = fastInRead()
    } while (c == ' '.toByte() || c == '\n'.toByte());

    var result = c - '0'.toByte()
    while (true) {
        c = fastInRead()
        if (c == (-1).toByte() || c == ' '.toByte() || c == '\n'.toByte()) {
            return result
        }
        result = result * 10 + c - '0'.toByte()
    }
}
