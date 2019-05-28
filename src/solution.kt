import java.io.*
import kotlin.concurrent.thread

val fastOutStream = FileOutputStream(FileDescriptor.out)
val fastOutBuf = run {
    Runtime.getRuntime().addShutdownHook(thread(start = false, name = "fast-io flush() shutdown hook", block = ::flush))
    ByteArray(4096)
}
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

val fastInStream = FileInputStream(FileDescriptor.`in`)
val fastInBuf = ByteArray(4096)
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
    val result = fastInBuf[fastInBufPos]
    fastInBufPos++
    return result
}

var fastInScanStringBuf: ByteArray? = null
@Suppress("deprecation")
fun scanString(): String {
    if (fastInScanStringBuf == null) fastInScanStringBuf = ByteArray(256)
    var c: Byte
    do {
        c = fastInRead()
    } while (c == ' '.toByte() || c == '\n'.toByte())

    var count = 0
    while (c != (-1).toByte() && c != ' '.toByte() && c != '\n'.toByte()) {
        if (count == fastInScanStringBuf!!.size) {
            val oldBuf = fastInScanStringBuf!!
            fastInScanStringBuf = ByteArray(oldBuf.size * 2)
            System.arraycopy(oldBuf, 0, fastInScanStringBuf, 0, count)
        }
        fastInScanStringBuf!![count] = c
        count++
        c = fastInRead()
    }
    return java.lang.String(fastInScanStringBuf, 0, 0, count) as String
}
fun scanInt(): Int {
    var c: Byte
    do {
        c = fastInRead()
    } while (c == ' '.toByte() || c == '\n'.toByte())

    val negative = c == '-'.toByte()
    if (negative) c = fastInRead()

    var result = 0
    while (c != (-1).toByte() && c != ' '.toByte() && c != '\n'.toByte()) {
        result = result * 10 + c - '0'.toByte()
        c = fastInRead()
    }
    return if (negative) -result else result
}
fun scanLong(): Long {
    var c: Byte
    do {
        c = fastInRead()
    } while (c == ' '.toByte() || c == '\n'.toByte())

    val negative = c == '-'.toByte()
    if (negative) c = fastInRead()

    var result = 0.toLong()
    while (c != (-1).toByte() && c != ' '.toByte() && c != '\n'.toByte()) {
        result = result * 10 + c - '0'.toByte()
        c = fastInRead()
    }
    return if (negative) -result else result
}
