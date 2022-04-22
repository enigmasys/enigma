package edu.vanderbilt.enigma.command

import com.fasterxml.jackson.databind.ObjectMapper
import edu.vanderbilt.enigma.model.observation.EgressResult
import edu.vanderbilt.enigma.services.FileUploader
import edu.vanderbilt.enigma.services.ObservationServiceImpl
import edu.vanderbilt.enigma.services.PremonitionProcessServiceImpl
import edu.vanderbilt.enigma.util.prettyJsonPrint
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.io.File
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

    override fun call(): Int {
    when{
        help -> exitProcess(0)
        else ->{

            val expiresInMins = "600"
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
                prettyJsonPrint(it)
                val mapper = ObjectMapper()
                val nFile = "$dir/$startObsIndex/observation.json"
//                val patt = "$outputDir/obervation$i.json"
                val file = File(nFile)
                file.createNewFile()
                mapper.writeValue(file,it.data)
            }

            val result = ObservationDownloadServiceObj.getObservationFilesV3(processID, startObsIndex!!, endObsIndex!!, expiresInMins)

            val values = result as EgressResult
            ObservationDownloadServiceObj.DownloadFiles(values,dir)
        }
    }
        return 0

    }
}