package command

import common.model.process.ProcessOwned
import common.services.PremonitionProcessServiceImpl
import common.services.auth.AuthService
import common.util.prettyJsonPrint
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
    name = "process",
    aliases = ["proc","repository","repo"],
    mixinStandardHelpOptions = true,
)
class ProcessCmd(
    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val AuthServiceObj: AuthService

)
: Callable<Int>
{

    @CommandLine.Option(names = ["-l", "--listofProcesses","--list"], description = ["Display the list of owned Repositories(a.k.a Process)."])
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
                val result =  ProcessServiceObj.getListofProcesses() as ProcessOwned

                println("=============================================")
                println("Repository ID                         | Content Type | Description              | Is Function")
                if (result.size > 0){
                    result.forEach {
                        println( it.processId + "  |  " + it.processType + "  |  " + it.description + "  |  " + it.isFunction)
                    }
                }
                println("=============================================")

            }
                else -> 0
        }
        return 0
    }

}