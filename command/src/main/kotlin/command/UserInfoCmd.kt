package command



import common.services.PremonitionProcessServiceImpl
import common.services.UserInfo
import common.services.auth.AuthService
import common.util.prettyJsonPrint
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
    name = "userinfo",
    aliases = ["user"],
    mixinStandardHelpOptions = true,
)
class UserInfoCmd(
    private val UserInfoObj: UserInfo,
    private val AuthServiceObj: AuthService

)
    : Callable<Int>
{

    @CommandLine.Option(names = ["-s", "--status"], description = ["Display the Status of this Application/User."])
    var appStatus = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }

        when{
            appStatus ->{
                val result =  UserInfoObj.getUserRegistration()
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