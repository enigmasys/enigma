package command

import common.services.UserInfo
import common.services.auth.AuthService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
    name = "userinfo",
    aliases = ["user"],
    mixinStandardHelpOptions = true,
)
class UserInfoCmd(
    private val UserInfoObj: UserInfo,
    private val AuthServiceObj: AuthService,
) :
    Callable<Int> {
    @CommandLine.Option(names = ["-s", "--status", "--login"], description = ["Display the Status of this Application/User."])
    var appStatus = false

    @CommandLine.Option(names = ["-o", "--logout"], description = ["Logout from this Application"])
    var loginFun = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    @Value("\${cliclient.TOKEN_CACHE_FILE_PATH:.token_cache.json}")
    private val TOKEN_CACHE_FILE_PATH: String = ""

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?: 0 > 0)
                {
                    parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
                }
        }

        when {
            loginFun -> {
                println("Login Functionality..")
                // Here we check if the TOKEN_CACHE_FILE_PATH is present or not from the cliclient application properties
                val file: File = ResourceUtils.getFile(TOKEN_CACHE_FILE_PATH)

                if (file.exists())
                    {
//                    println("file present ${file.absolutePath}")
                        if (file.isFile) {
                            val result = file.delete()
                            when (result) {
                                true -> println("User Logged out successful")
                                false -> println("logout failed")
                            }
                        }
                    } else {
                    println("User is logged out already")
                }
            }

            appStatus -> {
                UserInfoObj.getUserRegistration()
//                if (result != null) {
// //                    prettyPrint(result)
//                    prettyJsonPrint(result)
//                }
            }
            else -> 0
        }
        return 0
    }
}
