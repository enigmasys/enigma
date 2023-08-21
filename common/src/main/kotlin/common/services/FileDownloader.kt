package common.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths


class FileDownloader {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)



        fun encodeParameterAndQuery(url: String): String {
            val uri = URL(url)
            val protocol = uri.protocol
            val host = uri.host
            val path = uri.path
            val query = uri.query

//            val encodedPath = path.split("/").joinToString("/") {
//                URLEncoder.encode(it, "UTF-8")
//            }

            val encodedPath = path.replace(" ", "%20")

            val encodedUrl = StringBuilder()
            encodedUrl.append(protocol).append("://").append(host).append(encodedPath)
            if (query != null) {
                encodedUrl.append("?").append(query)
            }
            return encodedUrl.toString()
        }


        suspend fun downloadFile(url: String, destination: String) {
            withContext(Dispatchers.IO) {
                try {

                    if (Files.notExists(Paths.get(destination)))
                        Files.createFile(Paths.get(destination))

                    val isPresent = async { isAvailable(url, destination)}.await()
                    if (!isPresent) {
                        logger.info("Starting download to $destination")
                        logger.info("Downloading from $url")
                        val encodedUrl = encodeParameterAndQuery(url)
                        val urlConnection = URL(encodedUrl).openConnection()
                        val inputStream = BufferedInputStream(urlConnection.getInputStream())


                        val outputStream = FileOutputStream(destination)
                        val data = ByteArray(1024)
                        var count = inputStream.read(data, 0, 1024)
                        while (count != -1) {
                            outputStream.write(data, 0, count)
                            count = inputStream.read(data, 0, 1024)
                        }
                        logger.info("Finished download to $destination")
                        outputStream.close()
                        inputStream.close()





                    }

                } catch (e: Exception) {
                    // Handle the exception here, or re-throw it
                    e.printStackTrace()
                }
            }
        }


        suspend fun getFileSizeOfUrl(url: String): Long {
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

        suspend fun isAvailable(fileURL: String?, localFilename: String?): Boolean {
            if (Files.exists(Paths.get(localFilename))) {
                val remoteSize = fileURL?.let { getFileSizeOfUrl(it) }
                val localSize = Files.size(Paths.get(localFilename))
                if (localSize != remoteSize) {
                    return false
                }
                return true
            } else
                return false
        }
    }
}