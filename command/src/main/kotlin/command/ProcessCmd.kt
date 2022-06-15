package command

import common.services.PremonitionProcessServiceImpl
import common.services.auth.AuthService
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
    private val AuthServiceObj: AuthService

)
: Callable<Int>
{

    @CommandLine.Option(names = ["-l", "--listofProcesses"], description = ["Display the list of owned processes."])
    var listofProcesses = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    override fun call(): Int {

        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
            parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
        } }

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