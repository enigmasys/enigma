package command



import common.services.PremonitionProcessServiceImpl
import common.services.UserInfo
import common.services.auth.AuthService
import common.util.prettyJsonPrint
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils
import picocli.CommandLine
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
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

    @CommandLine.Option(names = ["-s", "--status","--login"], description = ["Display the Status of this Application/User."])
    var appStatus = false

    @CommandLine.Option(names = ["-o", "--logout"], description = ["Logout from this Application"])
    var loginFun = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }

        when{

            loginFun -> {
                println("Login Functionality..")

                val file: File = ResourceUtils.getFile(".sample_cache.json")

                if(file.exists()){
//                    println("file present ${file.absolutePath}")
                    if (file.isFile) {
                        val result = file.delete()
                        when (result) {
                            true -> println("User Logged out successful")
                            false -> println("logout failed")
                        }
                    }

                }
                else {
                    println("User is logged out already")
                }

            }

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