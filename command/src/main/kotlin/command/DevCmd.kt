package command

//import common.services.PremonitionProcessServiceImpl
import common.services.TestClientService
import common.services.UserInfo
import common.util.prettyJsonPrint
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
    name = "dev",
    aliases = ["test"],
    mixinStandardHelpOptions = true,
)
class DevCmd(
    private val testClientServiceObj: TestClientService,
)
    : Callable<Int>
{

    @CommandLine.Option(names = ["-l", "--list"], description = ["Display process list"])
    var procStatus = false

    override fun call(): Int {
        when{
            procStatus ->{
                print("hello")
                val result =  testClientServiceObj.getTestMessage()
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