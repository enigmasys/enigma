package common.services

import com.azure.storage.blob.BlobContainerClientBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.model.Directory
import common.model.observation.UploadObservationObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class FileUploader(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val ObservationUploadServiceObj: ObservationUploadServiceImpl,
    private val ObservationDownloadServiceObj: ObservationServiceImpl

) {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun put(sasUrl: String, uploadDir: String,index:String){
        if (Files.notExists(Paths.get(uploadDir))) {
            logger.info("Folder does not exist")
            return
        }

        var blobContainerClient = BlobContainerClientBuilder().
        endpoint(sasUrl).buildClient()

        logger.info(blobContainerClient.blobContainerName)

        var fileMap = getMapofRelativeAndAbsolutePath(uploadDir)

        fileMap.forEach { (key, value) ->
            run {
                logger.info("$key: $value")
                val blobClient = blobContainerClient.getBlobClient("$index/$key")
                try {
                    blobClient.uploadFromFile(value.toString(),true)
                    logger.info("Finished Uploading $value")
                } catch (ex: UncheckedIOException) {
                    System.err.printf("Failed to upload from file %s%n", ex.message)
                }
            }
        }
    }


    fun uploadDirectory(processID: String, observerID: String, uploadDir: Path, data: Path? = null) {

        var uploadMetaData = generateUploadMetaData(processID, observerID = observerID, data) as UploadObservationObject
        val processInfo = ProcessServiceObj.getProcessState(processID)
        uploadMetaData.isFunction = processInfo!!.isFunction
        uploadMetaData.index = processInfo!!.numObservations
        uploadMetaData.processType = processInfo!!.processType
        var relativeFilePathList = getMapofRelativeAndAbsolutePath(uploadDir.toString()).keys
        uploadMetaData.dataFiles = relativeFilePathList.map { "${uploadMetaData.index}/$it" }.toList()
        ObservationUploadServiceObj.appendObservation(uploadMetaData)
        logger.info(uploadMetaData.toString())
        //
        val result = ObservationDownloadServiceObj.createTemporaryDirectory(processID, isUpload = true)
        val values = result as Directory
        put(values.sasUrl, uploadDir.toString(),uploadMetaData.index.toString())
        // putobservation
        uploadMetaData.dataFiles?.let {
            ObservationDownloadServiceObj.putObservationFiles(
                processID, result.directoryId, uploadMetaData.index.toString(),
                uploadMetaData.index.toString(), "0", it
            )
        }
    }

    companion object{
        fun getMapofRelativeAndAbsolutePath(uploadDir: String): Map<Path, Path> {

            var uploadDirPath = Paths.get(uploadDir)

            var fileList = Files.walk(uploadDirPath)
                .filter(Files::isRegularFile)
                .toList()

            var relativeFileList = fileList.map { uploadDirPath.relativize(it) }


            var fileMap = relativeFileList.zip(fileList).toMap()
            return fileMap

        }

        private fun generateUploadMetaData(processID:String, observerID:String,data:Path?) : Any {
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
            var uploadObs: UploadObservationObject = observationMapper.readValue(uploadData)
            uploadObs.data = emptyList()
            data?.let {
                uploadObs.data = listOf( observationMapper.readValue(it.toFile()))
            }

//        prettyPrint( observationMapper.writeValueAsString(uploadObs))
            return uploadObs
        }


    }
}