package edu.vanderbilt.enigma.services

import com.azure.storage.blob.BlobContainerClientBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.vanderbilt.enigma.model.Directory
import edu.vanderbilt.enigma.model.observation.UploadObservationObject
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileUploader(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val ObservationUploadServiceObj: ObservationUploadServiceImpl,
    private val ObservationDownloadServiceObj: ObservationServiceImpl

) {

    companion object{
        private val ProcessServiceObj: PremonitionProcessServiceImpl = FileUploader.ProcessServiceObj
        private val ObservationUploadServiceObj: ObservationUploadServiceImpl = FileUploader.ObservationUploadServiceObj
        private val ObservationDownloadServiceObj: ObservationServiceImpl = FileUploader.ObservationDownloadServiceObj

        fun getMapofRelativeAndAbsolutePath(uploadDir: String): Map<Path, Path> {

            var uploadDirPath = Paths.get(uploadDir)

            var fileList = Files.walk(uploadDirPath)
                .filter(Files::isRegularFile)
                .toList()

            var relativeFileList = fileList.map { uploadDirPath.relativize(it) }


            var fileMap = relativeFileList.zip(fileList).toMap()
            return fileMap

        }

        fun uploadDirectory(processID: String, observerID: String, uploadDir: Path) {
            var uploadMetaData = generateUploadMetaData(processID, observerID = observerID) as UploadObservationObject
            uploadMetaData.index = ProcessServiceObj.getProcessState(processID)!!.numObservations
            var relativeFilePathList = FileUploader.getMapofRelativeAndAbsolutePath(uploadDir.toString()).keys
            uploadMetaData.dataFiles = relativeFilePathList.map { it.toString() }.toList()
            ObservationUploadServiceObj.appendObservation(uploadMetaData)
            println(uploadMetaData)
            //
            val result = ObservationDownloadServiceObj.createTemporaryDirectory(processID, isUpload = true)
            val values = result as Directory
            put(values.sasUrl, uploadDir.toString())
            // putobservation
            uploadMetaData.dataFiles?.let {
                ObservationDownloadServiceObj.putObservationFiles(
                    processID, result.directoryId, uploadMetaData.index.toString(),
                    uploadMetaData.index.toString(), "0", it
                )
            }
        }

        private fun generateUploadMetaData(processID:String, observerID:String) : Any {
//		var processID = "4935ff85-8e84-4b06-a69a-9ac160542a50"
            val uploadData = """
            {
              "isFunction": false,
              "processType": "TestSim",
              "processId": "$processID",
              "isMeasure": true,
              "index": 1,
              "version": 0,
              "observerId": "$observerID",
              "startTime": "",
              "endTime": "",
              "applicationDependencies": [],
              "processDependencies": [],
              "data": [],
              "dataFiles": []
            }
        """.trim()
            val observationMapper = jacksonObjectMapper()
            var uploadObs:UploadObservationObject = observationMapper.readValue(uploadData)
            uploadObs.data = emptyList()
//        prettyPrint( observationMapper.writeValueAsString(uploadObs))
            return uploadObs
        }

        fun put(sasUrl: String, uploadDir: String){
            if (Files.notExists(Paths.get(uploadDir))) {
                println("Folder does not exist")
                return
            }

            var blobContainerClient = BlobContainerClientBuilder().
            endpoint(sasUrl).buildClient()

            println(blobContainerClient.blobContainerName)

            var fileMap = this.getMapofRelativeAndAbsolutePath(uploadDir)

            fileMap.forEach { (key, value) ->
                run {
                    println("$key: $value")
                    val blobClient = blobContainerClient.getBlobClient(key.toString())
                    try {
                        blobClient.uploadFromFile(value.toString(),true)
                        println("Finished Uploading $value")
                    } catch (ex: UncheckedIOException) {
                        System.err.printf("Failed to upload from file %s%n", ex.message)
                    }
                }
            }
        }
    }
}