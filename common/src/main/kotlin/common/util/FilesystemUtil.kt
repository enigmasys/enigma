package common.util

import java.io.File
import java.nio.file.Paths

fun tryExtendPath(dir: String): String {

    return if (!Paths.get(dir).isAbsolute) {
        var path = when (dir.startsWith("~" + File.separator)) {
            true -> Paths.get(System.getProperty("user.home") + dir.substring(1)).normalize().toString()
            false -> Paths.get(dir).normalize().toString()
        }
        Paths.get(path).toString()
    } else
        Paths.get(dir).normalize().toString()
}
