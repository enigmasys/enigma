package command



import common.services.PremonitionProcessServiceImpl
import common.services.UserInfo
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
)
    : Callable<Int>
{

    @CommandLine.Option(names = ["-s", "--status"], description = ["Display the Status of this Application/User."])
    var appStatus = false

    override fun call(): Int {
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