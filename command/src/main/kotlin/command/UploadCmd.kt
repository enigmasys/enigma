package command

import common.services.FileUploader
import common.services.UserInfo
import common.services.auth.AuthService
import common.util.JSONSchemaValidator
import common.util.ResourceSourceType
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
    private val JSONSchemaValidatorObj: JSONSchemaValidator


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


//    @CommandLine.ArgGroup(exclusive = false,multiplicity = "0..1")
//    var dependent: Dependent? = null

//    class Dependent {
//        @CommandLine.Option(names = ["-validate"], required = true)
//        var validate:Boolean = false
//
//        @CommandLine.Option(names = ["-type"], required = true, description = ["Taxonomy input source can be either of type either url or file"])
//        var type = ""
//        @CommandLine.Option(names = ["-path"], required = true, description = ["Taxonomy input source path - (file path/ URL address)"])
//        var path = ""
//    }


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

//                dependent?.validate?.let {
//                    println("Validate Flags is set to ${dependent?.validate}")
//                    println("JSONSchema Path Type: ${dependent?.type}")
//                    println("JSONSCHEMA Path: ${dependent?.path}")
//                    var validationResult:Boolean = false
//                    when(dependent?.type){
//                        "file" -> {
//                            var jsonSchemaFilePath: Path? =  dependent?.path?.run {
//                                when(Paths.get(dependent?.path).isAbsolute){
//                                    false -> Paths.get(dependent?.path).toAbsolutePath().normalize()
//                                    else -> Paths.get(dependent?.path)
//                                }
//                            }?:null
//                             validationResult = JSONSchemaValidatorObj.validate(
//                                ResourceSourceType.FILE,
//                                jsonSchemaFilePath.toString(),
//                                jsonFilePath.toString()
//                            )
//                        }
//                        "url"->{
//                            validationResult = JSONSchemaValidatorObj.validate(
//                                ResourceSourceType.URL,
//                                dependent?.path.toString(),
//                                jsonFilePath.toString()
//                            )
//                        }
//                    }
//                    if (!validationResult)
//                        exitProcess(0)
//                }

                val uploadDir = Paths.get(dir?.let { tryExtendPath(it) })
//                val uploadDir = when(Paths.get(dir).isAbsolute){
//                    false ->
//                        Paths.get(dir).toAbsolutePath().normalize()
//                    else -> Paths.get(dir)
//                }

                var oid: String = UserInfoObj.getUserRegistration()!!.userId


//                var oid: String = if (observerID==null){
//                    UserInfoObj.getUserRegistration()!!.userId
//
//                } else
//                    observerID as String


                processID?.let {
                    logger.info("$it ::  $oid  ::  $uploadDir :: $jsonFilePath")
                    FileUploaderObj.uploadDirectory(it, oid, uploadDir, jsonFilePath)
                }
            }
        }
        return 0
    }


}