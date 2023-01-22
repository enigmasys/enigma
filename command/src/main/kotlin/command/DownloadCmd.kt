package command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import common.model.observation.EgressResult
import common.model.observation.TaxonomyData
import common.model.observation.UploadObservationObject
import common.services.FileUploader
import common.services.ObservationServiceImpl
import common.services.PremonitionProcessServiceImpl
import common.services.TaxonomyServerClient
import common.services.auth.AuthService
import common.util.prettyJsonPrint
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess


@Component
@CommandLine.Command(
    name = "download",
    aliases = ["pull"],
    mixinStandardHelpOptions = true,
)
class DownloadCmd(
    private val ObservationDownloadServiceObj: ObservationServiceImpl,
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val AuthServiceObj: AuthService


): Callable<Int>
{

    @CommandLine.Option(required = true, names=["-d","--dir"], description = ["Directory Path"])
    lateinit var dir: String

    @CommandLine.Option(required = true, names=["-p","--process", "-repo"], description = ["Repository ID (a.k.a. ProcessID) of the repository"])
    lateinit var processID: String

//    @CommandLine.Option(required = false, names=["-o","--oid"], description = ["Observer ID"])
//    var observerID:String? = null

    @CommandLine.Option(required = false, names=["-i","--index"], description = ["index of the record"])
    var obsIndex:String? = null


    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Helps in downloading of records from a repository."])
    var help = false

    @CommandLine.Option(required = false, names=["-m","--metadata"], description = ["Download ONLY all metadata files (without the data files)"])
    var peek = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }


        when{
        help -> exitProcess(0)
        peek -> {
            obsIndex = (ProcessServiceObj.getProcessState(processID)!!.numObservations -1).toString()
            obsIndex?.let {
                it -> when(it){
                    "-1" -> logger.info("No Observations for Process ID: $processID ")
                    else ->
                    {
                        val resultData = ObservationDownloadServiceObj.getAllPeekObservations(processID = processID,
                            version= "0",
                            endObsIndex = obsIndex.toString()
                        )
                        resultData?.let { it ->
                            resultData.forEach{ mt ->
                                saveMetadata(mt,mt.index.toString())
                            }
                            prettyJsonPrint(it)
                        }

                    }
                }
            }

        }
        else ->{

            val expiresInMins = "2"
            var startObsIndex:String? =  null
            var endObsIndex:String? = null

            if (obsIndex==null){
                // we need to fetch the last index of the processID and then fetch this observation.
                // we need to invoke the processState.
                obsIndex = (ProcessServiceObj.getProcessState(processID)!!.numObservations -1).toString()
                startObsIndex =obsIndex
                endObsIndex = obsIndex
            }
            else
            {
                startObsIndex = obsIndex
                endObsIndex = obsIndex
            }

            // We need to also download the Observation Meta Data.

            val resultData = ObservationDownloadServiceObj.getObservation(processID = processID,
                startObsIndex = startObsIndex!!,
                version= "0"
            )
            saveMetadata(resultData, startObsIndex)

            var result = ObservationDownloadServiceObj.getObservationFilesV3(processID,
                startObsIndex, endObsIndex!!, expiresInMins)

            val values = result as EgressResult

            var notFound:Boolean = true
//            println("TransferID: ${values.transferId}")
            println("Download Command Invoked.")
            println("=====================================")
            println("Downloading records from repository $processID")
            println("Waiting for transfer to start.... ")
            println("Download started.. ")
            values.transferId?.let {
                while (notFound){
                    var transferStatus = ObservationDownloadServiceObj.getTransferStat(
                        processID = processID,
                        transferId = values.transferId!!,
                        directoryID = values.directoryId
                    )
                    print(".")
//                    logger.info("No Observations for Process ID: $processID ")
                    when (transferStatus?.status){
                        "Succeeded" -> {
                            notFound=false
                        }
                        else -> {
                            notFound=true
                            Thread.sleep(1000)
                        }
                    }
                }
            }

            ObservationDownloadServiceObj.DownloadFiles(values,dir)
            println("=====================================")
            println("Download Operation Completed")
            println("=====================================")
        }
    }
        return 0

    }

    private fun saveMetadata(resultData: UploadObservationObject?, startObsIndex: String?) {
        resultData?.let {
    //                prettyJsonPrint(it)
            val mapper = ObjectMapper()
            val downloadDir = when (Paths.get(dir).isAbsolute) {
                false -> Paths.get(dir).toAbsolutePath().normalize()
                else -> Paths.get(dir)
            }
            val nFile = "$downloadDir/metadata/$startObsIndex/metadata.json"
            var tmpTags = mapper.convertValue(it.data, Array<TaxonomyData>::class.java)[0].taxonomyTags
            val processInfo = ProcessServiceObj.getProcessState(processID)
            var index0_data = ObservationDownloadServiceObj.getObservation(
                processID = processID,
                startObsIndex = 0.toString(),
                version = processInfo?.lastVersionIndex.toString()
            )
            //        mapper.readValue<TaxonomyData>(mapper.writeValueAsString(teest))
    //        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,false)
            //        val teest: List<TaxonomyData> = index0_data.data as List<TaxonomyData>
            val tmpTaxonomyData = mapper.convertValue(index0_data!!.data, Array<TaxonomyData>::class.java)
    //        val tmpTaxonomyData:TaxonomyData = mapper.convertValue(index0TaxonomyData,jacksonTypeRef<List<TaxonomyData>>())[0]
    //        val tmpTaxonomyData:TaxonomyData = mapper.convertValue(index0TaxonomyData,jacksonTypeRef<List<TaxonomyData>>())[0]
            val displayName = tmpTaxonomyData[0].displayName
            // This will be the tagFormatter URL to be called for the given taxonomy data.
            var tmp_baseurl = ""
            if (!tmpTaxonomyData[0].taxonomyVersion!!.url?.contains("http")!!) {
                tmp_baseurl = "http://" + tmpTaxonomyData[0].taxonomyVersion!!.url
            } else
                tmp_baseurl = tmpTaxonomyData[0].taxonomyVersion!!.url.toString()


            if (tmpTags?.size!! > 0) {
                val tmpdata = tmpTaxonomyData[0].taxonomyVersion!!.branch?.let { it1 ->
                    tmpTaxonomyData[0].taxonomyVersion!!.id?.let { it2 ->
                        TaxonomyServerClient().getHumanTags(
                            tmp_baseurl, it2,
                            it1, mapper.writeValueAsString(tmpTags)
                        )
                    }
                }
                if (tmpdata != null) {
                    tmpTaxonomyData[0].taxonomyTags = mapper.readValue(tmpdata)
                    //                    (it.data?.get(0) as TaxonomyData).taxonomyTags = ObjectMapper().readTree(tmpdata).toList()
    //                    (it.data?.get(0) as TaxonomyData).taxonomyTags = mapper.readValue(tmpdata!!)
                }
            }

            it.data = tmpTaxonomyData.toList()
            val file = File(nFile)
            val tmpDir = Paths.get(nFile).parent

            if (Files.notExists(tmpDir))
                Files.createDirectories(tmpDir)
            println("Saving metadata to ${file.absolutePath}")
            file.createNewFile()
            mapper.writeValue(file, (it.data as List<*>)[0])
        }
    }
}