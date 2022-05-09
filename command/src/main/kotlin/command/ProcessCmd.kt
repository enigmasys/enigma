package command

import common.services.PremonitionProcessServiceImpl
import common.util.prettyJsonPrint
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
    name = "process",
    aliases = ["proc"],
    mixinStandardHelpOptions = true,
)
class ProcessCmd(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    )
: Callable<Int>
{

    @CommandLine.Option(names = ["-l", "--listofProcesses"], description = ["Display the list of owned processes."])
    var listofProcesses = false

    override fun call(): Int {
        when{
            listofProcesses ->{
                val result =  ProcessServiceObj.getListofProcesses()
                if (result != null) {
//                    prettyPrint(result)
                    prettyJsonPrint(result)
                }
            }
                else -> 0
        }
        return 0
    }

}