package common.services

import com.azure.storage.blob.BlobContainerClientBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import common.model.Directory
import common.model.observation.TaxonomyData
import common.model.observation.UploadObservationObject
import org.objectweb.asm.TypeReference
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
    private val ObservationDownloadServiceObj: ObservationServiceImpl,
    private val TaxonomyServerClientObj:TaxonomyServerClient

) {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun put(sasUrl: String, uploadDir: String, index: String) {
        if (Files.notExists(Paths.get(uploadDir))) {
            logger.info("Folder does not exist")
            return
        }

        var blobContainerClient = BlobContainerClientBuilder().endpoint(sasUrl).buildClient()

//        logger.info(blobContainerClient.blobContainerName)

        var fileMap = getMapofRelativeAndAbsolutePath(uploadDir)

        fileMap.forEach { (key, value) ->
            run {
                logger.info("$key: $value")
                println("Uploading File: $value")
                val blobClient = blobContainerClient.getBlobClient("$index/$key")
                try {
                    blobClient.uploadFromFile(value.toString(), true)
                    println("Finished Uploading: $value")
//                    logger.info("Finished Uploading $value")

                } catch (ex: UncheckedIOException) {
                    System.err.printf("Failed to upload from file %s%n", ex.message)
                }
            }
        }
    }


    fun uploadDirectory(processID: String, observerID: String, uploadDir: Path, data: Path? = null) {


        val processInfo = ProcessServiceObj.getProcessState(processID)

        /// We need to include the displayName, taxonomyVersion(To form the TagFormatter URL ).
        // for this we would need to fetch the displayName from Index 0.

//        http://localhost:12345/routers/TagFormat/aadid_yogesh_p_d_p_barve_at_vanderbilt_p_edu%2BLEAP_Taxonomy_Release_v1B/branch/master/human?tags=
//
        var index0_data = ObservationDownloadServiceObj.getObservation(processID = processID,
                                startObsIndex = 0.toString(),
                                version= processInfo?.lastVersionIndex.toString()
            )

//        mapper.readValue<TaxonomyData>(mapper.writeValueAsString(teest))
//        mapper.readValue<TaxonomyData>(mapper.writeValueAsString(teest[0]),TaxonomyData::class.java)
        var mapper =  ObjectMapper(); // or inject it as a dependency
//        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,false)

//        val teest: List<TaxonomyData> = index0_data.data as List<TaxonomyData>
        val tmpTaxonomyData =   mapper.convertValue(index0_data!!.data,Array<TaxonomyData>::class.java)
//        val tmpTaxonomyData:TaxonomyData = mapper.convertValue(index0TaxonomyData,jacksonTypeRef<List<TaxonomyData>>())[0]
//        val tmpTaxonomyData:TaxonomyData = mapper.convertValue(index0TaxonomyData,jacksonTypeRef<List<TaxonomyData>>())[0]


        val displayName = tmpTaxonomyData[0].displayName
        // This will be the tagFormatter URL to be called for the given taxonomy data.
        var tmp_baseurl = ""
        if (!tmpTaxonomyData[0].taxonomyVersion!!.url?.contains("http")!!){
            tmp_baseurl = "http://"+tmpTaxonomyData[0].taxonomyVersion!!.url
        }else
            tmp_baseurl = tmpTaxonomyData[0].taxonomyVersion!!.url.toString()
        val toGuidFormatURL = tmp_baseurl + "/routers/TagFormat/"+ java.net.URLEncoder.encode(tmpTaxonomyData[0].taxonomyVersion!!.id, "utf-8")+"/branch/"+ tmpTaxonomyData[0].taxonomyVersion!!.branch+"/guid"

        val uploadMetaData = tmpTaxonomyData[0].taxonomyVersion!!.branch?.let { branchID ->
            tmpTaxonomyData[0].taxonomyVersion!!.id?.let { projectID ->
                generateUploadMetaData(processID, observerID = observerID, data,displayName,tmp_baseurl, projectID,
                    branchID
                )
            }
        } as UploadObservationObject

        uploadMetaData.isFunction = processInfo!!.isFunction
        uploadMetaData.index = processInfo.numObservations
        uploadMetaData.processType = processInfo.processType
        var relativeFilePathList = getMapofRelativeAndAbsolutePath(uploadDir.toString()).keys
        uploadMetaData.dataFiles = relativeFilePathList.map { "${uploadMetaData.index}/$it" }.toList()
        // TODO: Let's comment out for now
        ObservationUploadServiceObj.appendObservation(uploadMetaData)
        logger.info(uploadMetaData.toString())
//        //
        val result = ObservationDownloadServiceObj.createTemporaryDirectory(processID, isUpload = true)
        val values = result as Directory
        put(values.sasUrl, uploadDir.toString(), uploadMetaData.index.toString())
//        // putobservation
        uploadMetaData.dataFiles?.let {
            ObservationDownloadServiceObj.putObservationFiles(
                processID, result.directoryId, uploadMetaData.index.toString(),
                uploadMetaData.index.toString(), "0", it
            )
        }
    }

    companion object {
        fun getMapofRelativeAndAbsolutePath(uploadDir: String): Map<Path, Path> {

            var uploadDirPath = Paths.get(uploadDir)

            var fileList = Files.walk(uploadDirPath)
                .filter(Files::isRegularFile)
                .toList()

            var relativeFileList = fileList.map { uploadDirPath.relativize(it) }


            var fileMap = relativeFileList.zip(fileList).toMap()
            return fileMap

        }

        private fun generateUploadMetaData(processID: String, observerID: String, data: Path?, displayName: String?, ToGuidTagURL: String?, projectID: String, projectBranch: String): Any {
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

            var rawData: TaxonomyData? = null

            data?.let {

//                uploadObs.data = listOf(observationMapper.readValue(it.toFile()))
                rawData = observationMapper.readValue(it.toFile())
            }

//            uploadObs.data = emptyList()
            if (ToGuidTagURL != null) {
                val tmpdata =  TaxonomyServerClient().getGuidTags(ToGuidTagURL, projectID , projectBranch, ObjectMapper().writeValueAsString(rawData!!.taxonomyTags))
                if (tmpdata != null) {
                    rawData?.taxonomyTags = ObjectMapper().readTree(tmpdata).toList()
                }
            }
            rawData?.displayName = displayName

            data?.let {
                //uploadObs.data = listOf(observationMapper.readValue(it.toFile()))
//                uploadObs.data = observationMapper.readValue(it.toFile())
                uploadObs.data = listOf(rawData as Any)
            }
//        prettyPrint( observationMapper.writeValueAsString(uploadObs))
            return uploadObs
        }


    }
}