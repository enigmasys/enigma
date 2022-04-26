//package edu.vanderbilt.enigma.controller
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import edu.vanderbilt.enigma.model.*
//import edu.vanderbilt.enigma.model.observation.UploadObservationObject
//import edu.vanderbilt.enigma.model.process.ProcessOwned
//import edu.vanderbilt.enigma.services.ObservationServiceImpl
//import edu.vanderbilt.enigma.services.ObservationUploadServiceImpl
//import edu.vanderbilt.enigma.services.PremonitionFileUploadService
//import edu.vanderbilt.enigma.services.PremonitionProcessServiceImpl
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
//import org.springframework.context.annotation.Profile
//import org.springframework.core.io.ResourceLoader
//import org.springframework.http.*
//import org.springframework.web.bind.annotation.*
//
//
//@RestController
//class HelloController(
//    private val ProcessServiceObj: PremonitionProcessServiceImpl,
//    private val ObservationServiceObj: ObservationServiceImpl,
//    private val ObservationUploadServiceObj: ObservationUploadServiceImpl,
//    private var resourceLoader: ResourceLoader,
//    private val uploadService: PremonitionFileUploadService
//
//
//) {
//    @Value("\${base_url:https://premonition.azurewebsites.net/v2/}")
//    private lateinit var baseUrl: String
//
//    @RequestMapping("/")
//    fun index() = "This is home!"
//
//    @GetMapping("Admin")
//    @ResponseBody
//    fun Admin(): String {
//        return "Admin message"
//    }
//
//    @GetMapping("/listprocs")
//    @ResponseBody
//    fun explicit(): ProcessOwned? {
//        return ProcessServiceObj.getListofProcesses()
//    }
//
//    @GetMapping("/createdirectory")
//    @ResponseBody
//    fun createDirectory(@RequestParam(name = "processId") processID: String): Directory? {
//        return ObservationServiceObj.createTemporaryDirectory(processID)
//    }
//
//    @GetMapping("/getobs")
//    @ResponseBody
//    fun getObservationFiles(
//        @RequestParam(name = "processId") processID: String,
//        @RequestParam(name = "directoryId") directoryID: String,
//        @RequestParam(name = "obsIndex") startObsIndex: String,
//        @RequestParam(name = "endObsIndex") endObsIndex: String
//    ): String? {
//        return ObservationServiceObj.getObservationFiles(
//            processID,
//            directoryID,
//            startObsIndex,
//            endObsIndex
//            )
//    }
//
//    @GetMapping("/getTransferStat")
//    @ResponseBody
//    fun getTransferStatus(
//        @RequestParam(name = "processId") processID: String,
//        @RequestParam(name = "directoryId") directoryID: String,
//        @RequestParam(name = "transferId") transferId: String
//    ): TransferStat? {
//        return ObservationServiceObj.getTransferStat(processID, directoryID, transferId)
//    }
//
//
//    @GetMapping("/GetObservations")
//    fun getObservationsV3(
//        @RequestParam(name = "processId") processID: String,
//        @RequestParam(name = "obsIndex") startObsIndex: String,
//        @RequestParam(name = "endObsIndex") endObsIndex: String,
//        @RequestParam(name = "expiresInMins") expiresInMins: String = "60",
//        @RequestParam(name = "download", defaultValue = "false") download: String
//    ): ResponseEntity<Any>? {
//        val response = ObservationServiceObj.getObservationsV3(
//            processID,
//            startObsIndex,
//            endObsIndex,
//            expiresInMins
//        )
//
//        var result: ResponseEntity<Any>? = null
//
//        when (download) {
//            "false" -> result = ResponseEntity.ok(response);
//            "true" -> {
//
////                val sasUrlList: ArrayList<String> = ArrayList<String>()
//                response?.dataLakeFiles?.forEach { it ->
////                    sasUrlList.add(it.sasUrl)
//
////                    .contentType(MediaType.parseMediaType(uploadedFileToRet.getFileType()))
////                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename= "+uploadedFileToRet.getFileName())
//                    val resource = resourceLoader.getResource(it.sasUrl)
//                    val header = HttpHeaders()
//                    header.contentDisposition = ContentDisposition
//                        .builder("attachment")
//                        .filename(resourceLoader.getResource(it.sasUrl).filename.toString())
//                        .build()
//
//                    result =
//                        ResponseEntity
//                            .ok().headers(header).body(resource)
//
//                }
//            }
//            else -> {
//                result = ResponseEntity.ok(response)
//            }
//
//        }
//        return result
//    }
//
//    // This gets a list of the different dataLakeFiles....
//    @GetMapping("/GetObservationFiles")
//    @ResponseBody
//    fun getObservationFilesV3(
//        @RequestParam(name = "processId") processID: String,
//        @RequestParam(name = "obsIndex") startObsIndex: String,
//        @RequestParam(name = "endObsIndex") endObsIndex: String,
//        @RequestParam(name = "expiresInMins") expiresInMins: String = "60"
//    ): String? {
//
//        return ObservationServiceObj.getObservationFilesV3(processID, startObsIndex, endObsIndex, expiresInMins)
//    }
//
//
//    @GetMapping("/GetObservation")
//    @ResponseBody
//    fun getObservation(
//        @RequestParam(name = "processId") processID: String,
//        @RequestParam(name = "obsIndex") startObsIndex: String,
//        @RequestParam(name = "version") version: String
//    ): String? {
//        return ObservationServiceObj.getObservation(processID, startObsIndex, version)
//    }
//
//
//    @GetMapping("/getProcessState", produces = [MediaType.APPLICATION_JSON_VALUE])
//    @ResponseBody
//    fun getProcessState(@RequestParam(name = "processId") processID: String): ProcessState? {
//        return ProcessServiceObj.getProcessState(processID)
//    }
//
//    //    @PostMapping("/appendObservation")
//    //    fun appendObservation( @RegisteredOAuth2AuthorizedClient("premonition") authorizedClient: OAuth2AuthorizedClient?,
//    //                           @RequestBody observationObject: UploadObservationObject?): String? {
//    //        return observationObject?.let { ObservationUploadServiceObj.appendObservation(it,authorizedClient) }
//    //    }
//
//    @GetMapping("/appendObservation")
//    @ResponseBody
//    fun appendObservation(): String? {
//        val mapper = jacksonObjectMapper()
//        val observationObject: UploadObservationObject = mapper.readValue(
//            "{\n" +
//                    "  \"isFunction\": false,\n" +
//                    "  \"processType\": \"testSim\",\n" +
//                    "  \"processId\": \"82abfdc8-7c78-4ae6-b137-a8fe1b4116d8\",\n" +
//                    "  \"isMeasure\": true,\n" +
//                    "  \"index\": 1,\n" +
//                    "  \"version\": 0,\n" +
//                    "  \"observerId\": \"b92dfdef-f13e-48f3-a56f-07f161f3aac2\",\n" +
//                    "  \"startTime\": \"\",\n" +
//                    "  \"endTime\": \"\",\n" +
//                    "  \"applicationDependencies\": [],\n" +
//                    "  \"processDependencies\": [],\n" +
//                    "  \"data\": [\n" +
//                    "    {\n" +
//                    "      \"School\": \"Tennessee State University\",\n" +
//                    "      \"City\": \"Nashville\",\n" +
//                    "      \"State\": \"Tennessee\",\n" +
//                    "      \"Country\": \"USA\"\n" +
//                    "    }\n" +
//                    "  ],\n" +
//                    "  \"dataFiles\": [\n" +
//                    "  ]\n" +
//                    "}"
//        )
//        return observationObject.let { ObservationUploadServiceObj.appendObservation(it) }
//    }
//
//
//    @PostMapping("/appendObs")
//    @ResponseBody
//    fun appendObservations(
//        observationObject: UploadObservationObject?
//    ): String? {
//        return "Success"
////        return observationObject?.let { ObservationUploadServiceObj.appendObservation(it,authorizedClient) }
//    }
//
//
////    @PostMapping(path = ["/upload"])
////    @ResponseBody
////    fun uploadFile(@RequestParam("file") multipartFile: MultipartFile, @RequestParam("url")): Mono<HttpStatus> {
////        return uploadService.uploadFile(multipartFile.resource)
////    }
////
////    @PostMapping(path = ["/upload/multipart"])
////    @ResponseBody
////    fun uploadMultipart(@RequestParam("file") multipartFile: MultipartFile?): Mono<HttpStatus>? {
////        return multipartFile?.let { uploadService.uploadMultipart(it) }
////    }
//
//}
