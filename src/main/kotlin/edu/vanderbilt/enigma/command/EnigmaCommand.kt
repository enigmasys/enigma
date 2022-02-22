package edu.vanderbilt.enigma.command


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import edu.vanderbilt.enigma.services.PremonitionProcessServiceImpl
import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable


@Component
@Command(
    name = "premcli",
    mixinStandardHelpOptions = true,
    version = ["premcli"],
    description = ["Command for premonition datalake"]
)
class EnigmaCommand(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,

//    val generatorService: GeneratorService,
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
        }

        return 0
    }
}