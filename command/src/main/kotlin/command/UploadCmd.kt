package command

import common.services.FileUploader
import common.services.UserInfo
import common.services.auth.AuthService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess
import common.util.tryExtendPath
import org.springframework.context.annotation.ComponentScan

@Component
@CommandLine.Command(
    name = "upload",
    aliases = ["push"],
    mixinStandardHelpOptions = true,
)
@ComponentScan(basePackages = ["common"])
class UploadCmd(
    private val FileUploaderObj: FileUploader,
    private val UserInfoObj: UserInfo,
    private val AuthServiceObj: AuthService,
): Callable<Int> {

    val logger = LoggerFactory.getLogger(this::class.java)

    @CommandLine.Option(required = true, names=["-d","--dir"], description = ["Directory Path"])
    var dir: String? = null

    @CommandLine.Option(required = true, names=["-p","--process","-repo"], description = ["Repository ID (a.k.a. ProcessID) of the repository"])
    var processID: String? = null
//
//    @CommandLine.Option(required = false, names=["-o","--oid"], description = ["Observer ID"])
//    var observerID:String? = null

    @CommandLine.Option(required = false, names=["-f"], description = ["JSON file path of metadata for the record"])
    var metadata:String? = null


    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Helps in uploading of records to a repository."])
    var help = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    override fun call(): Int  {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }

        when{
            help -> exitProcess(0)

            else -> {
                var jsonFilePath: Path? =  metadata?.run {
                    when(Paths.get(metadata).isAbsolute){
                        false -> Paths.get(metadata).toAbsolutePath().normalize()
                        else -> Paths.get(metadata)
                    }
                }?:null

                val uploadDir = Paths.get(dir?.let { tryExtendPath(it) })
                var oid: String = UserInfoObj.getUserRegistration()!!.userId

                println("Upload Command Invoked.")
                println("=====================================")
                println("Uploading records from $uploadDir to repository $processID")
                processID?.let {
                    logger.info("$it ::  $oid  ::  $uploadDir :: $jsonFilePath")
                    FileUploaderObj.uploadDirectory(it, oid, uploadDir, jsonFilePath)
                }
                println("Upload Complete")
                println("=====================================")
            }
        }
        return 0
    }


}