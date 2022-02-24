package edu.vanderbilt.enigma

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import edu.vanderbilt.enigma.model.observation.UploadObservationObject
import org.junit.jupiter.api.Test

class EnigmaApplicationTests {
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


}
