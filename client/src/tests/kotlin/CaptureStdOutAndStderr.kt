
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CaptureStdOutAndStderr : PrintStream(buffer) {
    val output: String
        get() = buffer.toString()

    // like a static ref
    companion object {
        private var parentStdout: PrintStream? = null
        private var parentStdErr: PrintStream? = null
        private val buffer = ByteArrayOutputStream()
    }

    init {
        if (parentStdout == null) {
            buffer.reset()
            parentStdout = System.out
            parentStdErr = System.err
            System.setOut(this)
            System.setErr(this)
        }
    }

    operator fun contains(search: String?): Boolean {
        return buffer.toString().contains(search)
    }

    override fun close() {
        super.close()
        if (parentStdout != null) {
            System.setOut(parentStdout)
            System.setOut(parentStdErr)
            parentStdout = null
            parentStdErr = null
        }
    }

    @SneakyThrows
    override fun write(b: Int) {
        write(byteArrayOf((b and 0xFF).toByte()))
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        buffer.write(b, off, len)
        parentStdout!!.write(b, off, len)
    }

    override fun flush() {
        parentStdout!!.flush()
    }


}