package command

import com.fasterxml.jackson.databind.ObjectMapper
import common.model.observation.EgressResult
import common.services.ObservationServiceImpl
import common.services.PremonitionProcessServiceImpl
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

    @CommandLine.Option(required = true, names=["-p","--process"], description = ["ProcessID"])
    lateinit var processID: String

    @CommandLine.Option(required = false, names=["-o","--oid"], description = ["Observer ID"])
    var observerID:String? = null

    @CommandLine.Option(required = false, names=["-i","--index"], description = ["Observer ID"])
    var obsIndex:String? = null


    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Utility for Test Commandline Options..."])
    var help = false

    @CommandLine.Option(required = false, names=["-m","--metadata"], description = ["Download all Observations(without datafiles)"])
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
//        peek -> {
//            obsIndex = (ProcessServiceObj.getProcessState(processID)!!.numObservations -1).toString()
//            obsIndex?.let {
//                it -> when(it){
//                    "-1" -> logger.info("No Observations for Process ID: $processID ")
//                    else ->
//                    {
//                        val resultData = ObservationDownloadServiceObj.getAllPeekObservations(processID = processID,
//                            version= "0",
//                            endObsIndex = obsIndex.toString()
//                        )
//                        resultData?.let {
//                            prettyJsonPrint(it)
//                        }
//
//                    }
//                }
//            }

//        }
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
            resultData?.let {
//                prettyJsonPrint(it)
                val mapper = ObjectMapper()

                val downloadDir = when(Paths.get(dir).isAbsolute){
                    false -> Paths.get(dir).toAbsolutePath().normalize()
                    else -> Paths.get(dir)
                }
                val nFile = "$downloadDir/$startObsIndex/observation.json"



//                val patt = "$outputDir/obervation$i.json"
                val file = File(nFile)

                val tmpDir = Paths.get(nFile).parent

                if (Files.notExists(tmpDir))
                    Files.createDirectories(tmpDir)
                file.createNewFile()
                mapper.writeValue(file,it)
            }

            var result = ObservationDownloadServiceObj.getObservationFilesV3(processID,
                startObsIndex, endObsIndex!!, expiresInMins)

            val values = result as EgressResult

            var notFound:Boolean = true
//            println("TransferID: ${values.transferId}")
            logger.info("Waiting for transfer to start.... ")
            values.transferId?.let {
                while (notFound){
                    var transferStatus = ObservationDownloadServiceObj.getTransferStat(
                        processID = processID,
                        transferId = values.transferId!!,
                        directoryID = values.directoryId
                    )
                    logger.info(".")
//                    logger.info("No Observations for Process ID: $processID ")
                    when (transferStatus?.status){
                        "Succeeded" -> {
                            logger.info("Download started.. ")

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
        }
    }
        return 0

    }
}