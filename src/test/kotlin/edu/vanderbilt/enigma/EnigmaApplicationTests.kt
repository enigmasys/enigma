package edu.vanderbilt.enigma

import com.azure.storage.blob.BlobContainerClientBuilder
import com.azure.storage.blob.BlobServiceClientBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.vanderbilt.enigma.model.observation.UploadObservationObject
import edu.vanderbilt.enigma.model.testdata.MRIData
import edu.vanderbilt.enigma.services.PremonitionProcessServiceImpl
import org.junit.Before
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class EnigmaApplicationTests() {
	@Test
	fun contextLoads() {
		val mapper = jacksonObjectMapper()
		val tmpData =
			"""
                    {
                    	"Modality": "MR",
                    	"MagneticFieldStrength": 3,
                    	"ImagingFrequency": 123.249,
                    	"Manufacturer": "Siemens",
                    	"ManufacturersModelName": "Verio",
                    	"InstitutionName": "Fudan_University_EENT_Hospital",
                    	"InstitutionalDepartmentName": "Department",
                    	"InstitutionAddress": "Fenyang_Road_83_Shanghai_8fa792_Xuhui_CN_ZIP",
                    	"DeviceSerialNumber": "40595",
                    	"StationName": "MRC40595",
                    	"PatientPosition": "HFS",
                    	"ProcedureStepDescription": "head_clinical_libraries",
                    	"SoftwareVersions": "syngo_MR_B17",
                    	"MRAcquisitionType": "3D",
                    	"SeriesDescription": "MPRAGE_3d_1x1x1",
                    	"ProtocolName": "MPRAGE_3d_1x1x1",
                    	"ScanningSequence": "GR_IR",
                    	"SequenceVariant": "SP_MP",
                    	"ScanOptions": "IR",
                    	"SequenceName": "_tfl3d1_ns",
                    	"ImageType": ["ORIGINAL", "PRIMARY", "M", "ND", "NORM"],
                    	"SeriesNumber": 74,
                    	"AcquisitionTime": "20:48:15.847500",
                    	"AcquisitionNumber": 1,
                    	"SliceThickness": 1,
                    	"SAR": 0.034897,
                    	"EchoTime": 0.00303,
                    	"RepetitionTime": 2,
                    	"InversionTime": 0.9,
                    	"FlipAngle": 9,
                    	"PartialFourier": 1,
                    	"BaseResolution": 256,
                    	"ShimSetting": [
                    		-2754,
                    		4830,
                    		-10388,
                    		364,
                    		-311,
                    		-367,
                    		-107,
                    		-119	],
                    	"TxRefAmp": 434.025,
                    	"PhaseResolution": 1,
                    	"ReceiveCoilName": "NeckMatrix",
                    	"CoilString": "C:HEA;HEP;NE2",
                    	"PulseSequenceDetails": "%SiemensSeq%_tfl",
                    	"PercentPhaseFOV": 100,
                    	"PercentSampling": 100,
                    	"PhaseEncodingSteps": 256,
                    	"AcquisitionMatrixPE": 256,
                    	"ReconMatrixPE": 256,
                    	"PixelBandwidth": 130,
                    	"DwellTime": 1.5e-05,
                    	"ImageOrientationPatientDICOM": [
                    		-0.013272,
                    		0.999912,
                    		-1.77184e-08,
                    		0.0700115,
                    		0.000929259,
                    		-0.997546	],
                    	"InPlanePhaseEncodingDirectionDICOM": "ROW",
                    	"ConversionSoftware": "dcm2niix",
                    	"ConversionSoftwareVersion": "v1.0.20200331"
                    }
                """.trimIndent()


		val uploadData = "{\n" +
				"  \"isFunction\": false,\n" +
				"  \"processType\": \"testSim\",\n" +
				"  \"processId\": \"82abfdc8-7c78-4ae6-b137-a8fe1b4116d8\",\n" +
				"  \"isMeasure\": true,\n" +
				"  \"index\": 1,\n" +
				"  \"version\": 0,\n" +
				"  \"observerId\": \"b92dfdef-f13e-48f3-a56f-07f161f3aac2\",\n" +
				"  \"startTime\": \"\",\n" +
				"  \"endTime\": \"\",\n" +
				"  \"applicationDependencies\": [],\n" +
				"  \"processDependencies\": [],\n" +
				"  \"data\": $tmpData," +
				"  \"dataFiles\": [\n" +
				"  ]\n" +
				"}"

		val observationObject: UploadObservationObject = mapper.readValue(uploadData)
//		val observationObject = mapper.readValue(uploadData)
		prettyPrint(observationObject)

	}


	private fun prettyPrint(input: Any) {
		val mapper = ObjectMapper()
		mapper.enable(SerializationFeature.INDENT_OUTPUT)
		val result = mapper.writeValueAsString(input)
		println(result)

	}

	@Test
	fun testUploadBlob(){
		var sasUrl="https://leapdevelopmentblob.blob.core.windows.net/22567be5-4f4e-4747-ae25-62d44a07fef9?sv=2020-04-08&si=UploadPolicy&sr=c&sig=dtuWKPa8ISBdhv8iBo7eY3CYxCB%2BqgYjUZ11IOqbfK0%3D"

		var bloburl = sasUrl.substringBefore("?")
		var blobsas = sasUrl.substringAfter("?")


		val path = Paths.get("").toAbsolutePath()
		val uploadDir = "$path/upload"
		if (Files.notExists(Paths.get(uploadDir)))
			println("Folder does not exist")
		println(Files.list(Paths.get(uploadDir)))


		val blobServiceClient = BlobServiceClientBuilder()
			.endpoint(bloburl)
			.sasToken(blobsas)
			.buildAsyncClient()
//		println(blobServiceClient.statistics)
//			.endpoint(sasUrl)
//			.buildClient()
//		println(blobClient.containerName)
	//		return blobClient.downloadStream(outputStream);

	}

	@Test
	fun testBlobClient()
	{

		var sasUrl="https://leapdevelopmentblob.blob.core.windows.net/7100ee9f-d975-4ab3-8719-8aeebeb25b4f?sv=2020-04-08&si=UploadPolicy&sr=c&sig=7772xvxbZSpuyIrzJEkVPdC6MgQCtmAHOs1lieQOnzU%3D"
//		var bloburl = sasUrl.substringBefore("?")
//		var blobsas = sasUrl.substringAfter("?")

		val path = Paths.get("").toAbsolutePath()
		val uploadDir = "$path/upload/"

		if (Files.notExists(Paths.get(uploadDir)))
			println("Folder does not exist")

//		val blobServiceClient = BlobServiceClientBuilder()
//			.endpoint(bloburl)
//			.sasToken(blobsas)
//			.buildClient();



		var blobContainerClient = BlobContainerClientBuilder().
				endpoint(sasUrl).buildClient()

		println(blobContainerClient.blobContainerName)




		var fileMap = getMapofRelativeAndAbsolutePath(uploadDir)

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


//		for file in relativeFileList:
//			println(file)

//        val blobClient = blobContainerClient.getBlobClient("data/test.txt")
//        blobClient.uploadFromFile(uploadDir)

//

//		println(blobServiceClient.toString())
//		blobServiceClient.
//		this.containerClient = serviceClient.
//		getBlobContainerClient(MY_DEFAULT_CONTAINER_NAME);
//
//		if (!this.containerClient.exists()) {
//			this.containerClient.create();
//		}
	}


	@Test
	fun TestgetRelativePaths(){
		val path = Paths.get("").toAbsolutePath()
		val uploadDir = Paths.get("$path/upload/")
		var fileList = Files.walk(uploadDir)
			.filter(Files::isRegularFile)
			.toList()
		var relativeFileList = fileList.map { uploadDir.relativize(it) }
		println(fileList)
		println(relativeFileList)
	}

	@Test
	fun TestgetFilesRelativePathList(){
		val path = Paths.get("").toAbsolutePath()
		val uploadDir = Paths.get("$path/upload/")
		var fileList = Files.walk(uploadDir)
			.filter(Files::isRegularFile)
			.toList()
		println(fileList)

	}

	@Test
	fun TestgetFilesPathList(){
		val path = Paths.get("").toAbsolutePath()
		val uploadDir = Paths.get("$path/upload/")
		var fileList = Files.walk(uploadDir)
			.filter(Files::isRegularFile)
			.toList()
		var relativeFileList = fileList.map { uploadDir.relativize(it) }
		println(relativeFileList)
	}


	fun getMapofRelativeAndAbsolutePath(uploadDir: String): Map<Path, Path> {
//		val path = Paths.get("").toAbsolutePath()
//		val uploadDir = Paths.get("$path/upload/")
		var uploadDirPath = Paths.get(uploadDir)

		var fileList = Files.walk(uploadDirPath)
			.filter(Files::isRegularFile)
			.toList()

		var relativeFileList = fileList.map { uploadDirPath.relativize(it) }


		var fileMap = relativeFileList.zip(fileList).toMap()
		return fileMap
//		println(relativeFileList)
//		return relativeFileList
	}

	fun getFilesPathList(uploadDir: String): List<Path>{
//		val path = Paths.get("").toAbsolutePath()
//		val uploadDir = Paths.get("$path/upload/")
		val uploadDirPath = Paths.get(uploadDir)
		var fileList = Files.walk(uploadDirPath)
			.filter(Files::isRegularFile)
			.toList()
//		println(fileList)
		return fileList
	}


	private fun generateUploadMetaData() : Any {
//		var processID = "4935ff85-8e84-4b06-a69a-9ac160542a50"
		val uploadData = """
            {
              "isFunction": false,
              "processType": "TestSim",
              "processId": "4935ff85-8e84-4b06-a69a-9ac160542a50",
              "isMeasure": true,
              "index": 1,
              "version": 0,
              "observerId": "d798e5be-344e-4e5e-994f-48d43e93d6d6",
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


	@Test
	fun testuploadObsforFile(){
		val uploadData:UploadObservationObject = generateUploadMetaData() as UploadObservationObject
//		val processStat = premonitionProcessObj.getProcessState(uploadData.processId)
		// use the observation count as the index for the observationID.....
//		val observationID = processStat?.numObservations
//		if (observationID != null) {
//			uploadData.index = observationID!!
//		}
		println(uploadData.index)
	}

}
