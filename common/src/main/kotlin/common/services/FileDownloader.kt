package common.services

import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths


class FileDownloader {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)

        @Throws(IOException::class)
        fun get(fileURL: String?, localFilename: String?) {
            println("Downloading file: $localFilename")
            val url = URL(fileURL)
            println("Remote File size ${fileURL?.let { getFileSizeOfUrl(it) }}")
            if (Files.exists(Paths.get(localFilename))) {
                println("Local File Size ${Files.size(Paths.get(localFilename))}")
            }

            val isPresent = isAvailable(fileURL, localFilename)
            if (!isPresent) {
                println("Starting Download..")
                if (Files.notExists(Paths.get(localFilename)))
                    Files.createFile(Paths.get(localFilename))
                try {
                    BufferedInputStream(URL(url.toString().replace(" ", "%20")).openStream()).use { `in` ->
                        FileOutputStream(localFilename).use { fileOutputStream ->
                            val dataBuffer = ByteArray(1024)
                            var bytesRead: Int
                            while (`in`.read(dataBuffer, 0, 1024).also { bytesRead = it } != -1) {
                                fileOutputStream.write(dataBuffer, 0, bytesRead)
                            }
                        }
                    }
                } catch (e: IOException) {
                    // handle exception
                    logger.error("exception caught:" + e)
                }
                println("Finished Downloading file: $localFilename")
            }
        }

        fun getFileSizeOfUrl(url: String): Long {
            var urlConnection: URLConnection? = null
            try {
                val uri = URL(url)
                urlConnection = uri.openConnection()
                urlConnection!!.connect()
                val contentLengthStr = urlConnection.getHeaderField("content-length")
                return if (contentLengthStr.isNullOrEmpty()) -1 else contentLengthStr.toLong()
            } catch (ignored: Exception) {
            } finally {
                if (urlConnection is HttpURLConnection)
                    urlConnection.disconnect()
            }
            return -1
        }

        fun isAvailable(fileURL: String?, localFilename: String?): Boolean {
//            logger.info("File size ${fileURL?.let { getFileSizeOfUrl(it) }}")
            val remoteSize = fileURL?.let { getFileSizeOfUrl(it) }
            var localSize: Long? = null
            if (Files.exists(Paths.get(localFilename))) {
//                logger.info("*******File Size ${Files.size(Paths.get(localFilename))}")
                localSize = Files.size(Paths.get(localFilename))
                if (localSize != remoteSize) {
                    return false
                }
                return true
            } else
                return false
        }
    }
}