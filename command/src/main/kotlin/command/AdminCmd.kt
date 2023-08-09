package command
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import common.model.process.ProcessOwned
import common.services.ObservationServiceImpl
import common.services.PremonitionProcessServiceImpl
import common.services.auth.AuthService
import common.util.prettyJsonPrint
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
    name = "admin",
//    aliases = ["proc","repository","repo"],
    mixinStandardHelpOptions = true,
)
class AdminCmd(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val ObservationServiceObj: ObservationServiceImpl,
    private val AuthServiceObj: AuthService

)
    : Callable<Int>
{
    @CommandLine.Option(names = ["-l", "--listofProcesses","--list"], description = ["Display the list of owned Repositories(a.k.a Process)."])
    var listofProcesses = false

    @CommandLine.Option(names = ["-dir"], description = ["Directory to save the downloaded files."], required = true)
    var dir: String? = null



    // Download raw metadata for each process
    // We could gather process ID from the list of processes commands and then download the raw metadata for each process.
    // we need process id and all the process.


    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }

        when{
            listofProcesses ->{
                val result =  ProcessServiceObj.getListofProcesses() as ProcessOwned

                println("=============================================")
                println("Repository ID                         | Content Type | Description              | Is Function")


                if (result.size > 0){
                    result.forEach {
                        println( it.processId + "  |  " + it.processType + "  |  " + it.description + "  |  " + it.isFunction)
                    }

                    result.forEach {

                        val processID = it.processId
                        val obsIndex = (ProcessServiceObj.getProcessState(processID)!!.numObservations -1).toString()

                        // surround this with try catch
                        try {
                            val rawMetadata = ObservationServiceObj.getAllPeekObservations(processID, version = "0", endObsIndex = obsIndex)

                            println("=============================================")
                            var jsonStr = jacksonObjectMapper().writeValueAsString(rawMetadata)
                            println(jsonStr)
                            // directory path
                            val tmpDir = dir?.let { Paths.get(it, processID) }

                            val fileName = Paths.get(tmpDir.toString(), "$processID.json").toString()
                            writeToJSONFile(jsonStr, fileName)


                        } catch (e: Exception){
                            println("Error while downloading metadata for process $processID")
                        }



//                    /Users/yogeshbarve/Projects/rest-tutorials/enigma/output


                    }

//                    val processID = "b20e07cd-a29f-4261-ba6e-bd1e33fa777a"

                    // write the jsonstr to a file



                    println("=============================================")
                }



                println("=============================================")

            }


            else -> 0
        }
        return 0
    }

}

fun writeToJSONFile(jsonStr: String, fileName: String){

    val file = File(fileName)
    val tmpDir = Paths.get(fileName).parent

    if (Files.notExists(tmpDir))
        Files.createDirectories(tmpDir)
    println("Saving metadata to ${file.absolutePath}")
    file.createNewFile()
    file.writeText(jsonStr)
}