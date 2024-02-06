package command

// import common.services.PremonitionProcessServiceImpl
import common.services.TestClientService
import common.services.auth.AuthService
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
    private val AuthServiceObj: AuthService,
) :
    Callable<Int> {
    @CommandLine.Option(names = ["-l", "--list"], description = ["Display process list"])
    var procStatus = false

//
    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?: 0 > 0)
                {
                    parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
                }
        }

        when {
            procStatus -> {
                print("hello")
                val result = testClientServiceObj.getTestMessage()
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
