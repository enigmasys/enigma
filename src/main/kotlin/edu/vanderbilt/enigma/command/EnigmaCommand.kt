package edu.vanderbilt.enigma.command


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.vanderbilt.enigma.services.PremonitionProcessServiceImpl
import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable


import edu.vanderbilt.enigma.model.observation.UploadObservationObject
import edu.vanderbilt.enigma.model.testdata.MRIData
import edu.vanderbilt.enigma.services.ObservationUploadServiceImpl


@Component
@Command(
    name = "premcli",
    mixinStandardHelpOptions = true,
    version = ["premcli"],
    description = ["Command for premonition datalake"]
)
class EnigmaCommand(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val ObservationUploadServiceObj: ObservationUploadServiceImpl,
) : Callable<Int> {

    @Option(names = ["-i","--inputDir"], description = ["Input Directory"],interactive = true)
    var inputDir: String? = null

    @Option(names = ["-o","--outputDir"], description = ["Output Directory Path"])
    var outputDir: String? = null

    @Option(names= ["-U","--upload"], description = ["Perform Upload Operation"])
    var uploadObs:Boolean = false

    @Option(names= ["-D","--download"], description = ["Perform Download Operation"])
    var downloadObs:Boolean = false

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["Print usage help and exit."])
    var usageHelpRequested = false

    @Option(names = ["-l", "--listofProcesses"], description = ["Display the list of owned processes."])
    var listofProcesses = false


    private fun prettyPrint(input: Any) {
        val mapper = ObjectMapper()
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        val result = mapper.writeValueAsString(input)
        println(result)

    }


    override fun call(): Int {

        when{
            listofProcesses -> {
                val result =  ProcessServiceObj.getListofProcesses()
                if (result != null) {
                    prettyPrint(result)
                }
            }
            uploadObs -> {

                val mapper = jacksonObjectMapper()
                val observationObject: UploadObservationObject = mapper.readValue("{\n" +
                        "  \"isFunction\": false,\n" +
                        "  \"processType\": \"testSim\",\n" +
                        "  \"processId\": \"82abfdc8-7c78-4ae6-b137-a8fe1b4116d8\",\n" +
                        "  \"isMeasure\": true,\n" +
                        "  \"index\": 1,\n" +
                        "  \"version\": 0,\n" +
                        "  \"observerId\": \"53f28719-1d33-4bad-b958-bc9537f3f42e\",\n" +
                        "  \"startTime\": \"\",\n" +
                        "  \"endTime\": \"\",\n" +
                        "  \"applicationDependencies\": [],\n" +
                        "  \"processDependencies\": [],\n" +
                        "  \"data\": [\n" +
                        "    {\n" +
                        "      \"School\": \"Tennessee State University\",\n" +
                        "      \"City\": \"Nashville\",\n" +
                        "      \"State\": \"Tennessee\",\n" +
                        "      \"Country\": \"USA\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"dataFiles\": [\n" +
                        "  ]\n" +
                        "}")


                val uploadData:UploadObservationObject = generateData() as UploadObservationObject
                ObservationUploadServiceObj.appendObservation(observationObject)
            }

//            downloadObs -> {
//
//
//            }


        }

        return 0
    }

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

        val uploadData = """
            {
              "isFunction": false,
              "processType": "testSim",
              "processId": "82abfdc8-7c78-4ae6-b137-a8fe1b4116d8",
              "isMeasure": true,
              "index": 1,
              "version": 0,
              "observerId": "b92dfdef-f13e-48f3-a56f-07f161f3aac2",
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
}