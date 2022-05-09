package edu.vanderbilt.enigma.services

import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths


class FileDownloader {

    companion object {
        @Throws(IOException::class)
        fun get(fileURL: String?, localFilename: String?) {
            println("Current file: $localFilename")
            val url = URL(fileURL)
            println("Remote File size ${fileURL?.let { getFileSizeOfUrl(it) }}")
            if(Files.exists(Paths.get(localFilename)))
            {
                println("Local File Size ${Files.size(Paths.get(localFilename))}")
            }

            val isPresent = this.isAvailable(fileURL, localFilename)
            if (!isPresent) {
                println("Starting Download..")
                if (Files.notExists(Paths.get(localFilename)))
                    Files.createFile(Paths.get(localFilename))
                Channels.newChannel(url.openStream()).use { readableByteChannel ->
                    FileOutputStream(localFilename).use { fileOutputStream ->
                        fileOutputStream.channel.use { fileChannel ->
                            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
                            fileOutputStream.close()
                        }
                    }
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

        fun isAvailable(fileURL: String?, localFilename: String? ) : Boolean{
            val url = URL(fileURL)
//            println("File size ${fileURL?.let { getFileSizeOfUrl(it) }}")
            val remoteSize =fileURL?.let { getFileSizeOfUrl(it)}
            var localSize: Long? = null
            if(Files.exists(Paths.get(localFilename)))
            {
//                println("*******File Size ${Files.size(Paths.get(localFilename))}")
                localSize = Files.size(Paths.get(localFilename))
                if (localSize !=remoteSize){
                    return false
                }
                return true
            }
            else
                 return false
        }
    }
}