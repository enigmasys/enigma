package command

import common.services.TaxonomyInfoService
import common.services.auth.AuthService
import common.util.tryExtendPath
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess

/**
 * Upload Command
 * @property TaxonomyInfoServceObj TaxonomyInfoService
 * @property logger (Logger..Logger?)
 * @property dir String?
 * @property processID String?
 * @property metadata String?
 * @property help Boolean
 * @property parent EnigmaCommand?
 * @property TAXONOMYPROJECT String
 * @property TAXONOMYBRANCH String
 * @constructor
 */
@Component
@CommandLine.Command(
    name = "upload",
    aliases = ["push"],
    mixinStandardHelpOptions = true,
)
@ComponentScan(basePackages = ["common"])
class UploadCmdv1(
    private val TaxonomyInfoServceObj: TaxonomyInfoService,
    private val AuthServiceObj: AuthService
): Callable<Int> {

    val logger = LoggerFactory.getLogger(this::class.java)

    @CommandLine.Option(required = true, names=["-d","--dir"], description = ["Directory Path"])
    var dir: String? = null

    @CommandLine.Option(required = true, names=["-p","--process","-repo"], description = ["Repository ID (a.k.a. ProcessID) of the repository"])
    var processID: String? = null

    @CommandLine.Option(required = false, names=["-f"], description = ["JSON file path of metadata for the record"])
    var metadata:String? = null

    @CommandLine.Option(required = false, names=["-m","-msg"], description = ["Add a description to the uploads"])
    var displayName:String? = null


    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Helps in uploading of records to a repository."])
    var help = false



    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    @Value("\${cliclient.taxonomyProject}")
    private val TAXONOMYPROJECT: String = "AllLeap+TaxonomyBootcamp123"
    @Value("\${cliclient.taxonomyBranch}")
    private val TAXONOMYBRANCH: String = "master123"

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

                println("Upload Command Invoked.")
                println("=====================================")
                println("Uploading records from $uploadDir to repository $processID")
                TaxonomyInfoServceObj.initTaxonomyInfoService(TAXONOMYPROJECT, TAXONOMYBRANCH)

                //Stress Test with 100 records

//                for (i in 1..10) {
                    processID?.let {
                        logger.info("$it ::  $uploadDir :: $jsonFilePath")
                        TaxonomyInfoServceObj.uploadToRepository(it, uploadDir, jsonFilePath, displayName)
                    }
//                }
                println("Upload Complete")
                println("=====================================")
            }
        }
        return 0
    }


}