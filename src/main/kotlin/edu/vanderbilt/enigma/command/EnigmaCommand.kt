package edu.vanderbilt.enigma.command


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.vanderbilt.enigma.model.observation.EgressResult
import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable


import edu.vanderbilt.enigma.model.observation.UploadObservationObject
import edu.vanderbilt.enigma.model.testdata.MRIData
import edu.vanderbilt.enigma.services.*
import edu.vanderbilt.enigma.util.prettyJsonPrint
import picocli.CommandLine
import java.io.File
import java.lang.Integer.max
import java.nio.file.Files
import java.nio.file.Paths



@Component
@Command(
    name = "premcli",
    mixinStandardHelpOptions = true,
    version = ["premcli"],
    description = ["Command for premonition datalake"],
    scope = CommandLine.ScopeType.LOCAL,
//    subcommands = [UploadCmd::class]
)
class EnigmaCommand(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val ObservationUploadServiceObj: ObservationUploadServiceImpl,
    private val ObservationDownloadServiceObj: ObservationServiceImpl,
    private val FileUploaderObj: FileUploader
//    private val FileDownloaderObj: FileDownloader


) : Callable<Int> {

    @Option(names = ["-i","--inputDir"], description = ["Input Directory"],interactive = true)
    var inputDir: String? = null

    @Option(names = ["-o","--outputDir"], description = ["Output Directory Path"])
    var outputDir: String? = null

    @Option(names= ["-U","--upoad"], description = ["Perform Upload Operation"])
    var uploadObs:Boolean = false

    @Option(names= ["-D","--download"], description = ["Perform Download Operation"])
    var downloadObs:Boolean = false

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["Print usage help and exit."])
    var usageHelpRequested = false

    @Option(names = ["-l", "--listofProcesses"], description = ["Display the list of owned processes."])
    var listofProcesses = false


    @Option(names=["-df","--downloadfiles"], description = ["Download Files of a process"])
    var downloadFiles = false

    @Option(names=["-uf","--uploadfiles"], description = ["Upload Files of a process"])
    var uploadFiles = false

    override fun call(): Int {
        when{
            uploadFiles ->{
                var processID = "4935ff85-8e84-4b06-a69a-9ac160542a50" // TestSim/
                var observerID = "d798e5be-344e-4e5e-994f-48d43e93d6d6"
//                val processID = "3d9adc35-e21e-43cc-b867-69b07305e75a"
                val startObsIndex = "0"
                val endObsIndex = "0"
                val expiresInMins = "60"
                // createobservation
                //Here we first create the observationMetaData
                val path = Paths.get("").toAbsolutePath()
                val uploadDir = Paths.get("$path/upload/dat")
                FileUploaderObj.uploadDirectory(processID, observerID, uploadDir)
            }

            downloadFiles ->{
                val processID = "3d9adc35-e21e-43cc-b867-69b07305e75a"
                val startObsIndex = "0"
                val endObsIndex = "0"
                val expiresInMins = "60"
                val result = ObservationDownloadServiceObj.getObservationFilesV3(processID, startObsIndex, endObsIndex, expiresInMins)
//                val response = ObservationDownloadServiceObj.getObservationsV3(
//            processID,
//            startObsIndex,
//            endObsIndex,
//            expiresInMins
//        )
            val values = result as EgressResult
            ObservationDownloadServiceObj.DownloadFiles(values)

//                val url = fileDownLoadMap.get("dat/0/col_source/dataset/120.xml")
//                val filePath = outputDir + "dat/0/col_source/dataset/120.xml"
//                val tmpDir = Paths.get(filePath).parent
//                if(Files.notExists(tmpDir))
//                    Files.createDirectories(tmpDir)
//
//                FileDownloader.get(url,filePath)


            }
            listofProcesses -> {
                val result =  ProcessServiceObj.getListofProcesses()
                if (result != null) {
//                    prettyPrint(result)
                    prettyJsonPrint(result)
                }
            }
            uploadObs -> {

                val mapper = jacksonObjectMapper()
                val uploadData:UploadObservationObject = generateData() as UploadObservationObject
                (0..100).forEach {
                    ObservationUploadServiceObj.appendObservation(uploadData)
//                    sleep(1)
                }

            }
            downloadObs -> {
//                val processID = "82abfdc8-7c78-4ae6-b137-a8fe1b4116d8"
                val processID = "4935ff85-8e84-4b06-a69a-9ac160542a50"

                val lastIndex = ProcessServiceObj.getProcessState(processID)!!.numObservations
                val startIndex = max(0,lastIndex-100)
                val endIndex = lastIndex

                // Download Observations to a folder...
                val path = Paths.get("").toAbsolutePath()
                val outputDir = "$path/output/"
                if(Files.notExists(Paths.get(outputDir)))
                  Files.createDirectory(Paths.get(outputDir))


                for(i in startIndex until endIndex){
                    val result = ObservationDownloadServiceObj.getObservation(processID = processID,
                        startObsIndex = i.toString(),
                        version= "0"
                    )
                    result?.let {
                        prettyJsonPrint(it)
                        val mapper = ObjectMapper()
                        val patt = "$outputDir/obervation$i.json"
                        val file = File(patt)
                        file.createNewFile()
                        mapper.writeValue(file,it.data)
                    }


                }

            }
        }

        return 0
    }

//    private fun uploadDirectory(processID: String, observerID: String, uploadDir: Path) {
//        var uploadMetaData = generateUploadMetaData(processID, observerID = observerID) as UploadObservationObject
//        uploadMetaData.index = ProcessServiceObj.getProcessState(processID)!!.numObservations
//        var relativeFilePathList = FileUploader.getMapofRelativeAndAbsolutePath(uploadDir.toString()).keys
//        uploadMetaData.dataFiles = relativeFilePathList.map { it.toString() }.toList()
//        ObservationUploadServiceObj.appendObservation(uploadMetaData)
//        println(uploadMetaData)
//        //
//        val result = ObservationDownloadServiceObj.createTemporaryDirectory(processID, isUpload = true)
//        val values = result as Directory
//        FileUploader.put(values.sasUrl, uploadDir.toString())
//        // putobservation
//        uploadMetaData.dataFiles?.let {
//            ObservationDownloadServiceObj.putObservationFiles(
//                processID, result.directoryId, uploadMetaData.index.toString(),
//                uploadMetaData.index.toString(), "0", it
//            )
//        }
//    }


    private fun generateData() : Any {
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
                """.trim()

        val mriDataMapper = jacksonObjectMapper()
        var mridata: MRIData = mriDataMapper.readValue(tmpData)


//        d798e5be-344e-4e5e-994f-48d43e93d6d6
//                "observerId": "53f28719-1d33-4bad-b958-bc9537f3f42e",
//                "processId": "82abfdc8-7c78-4ae6-b137-a8fe1b4116d8",



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
        uploadObs.data = listOf(mridata)
//        prettyPrint( observationMapper.writeValueAsString(uploadObs))
        return uploadObs
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
}