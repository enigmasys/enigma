package command

import common.services.FileUploader
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Component
@CommandLine.Command(
    name = "upload",
    aliases = ["push"],
    mixinStandardHelpOptions = true,
)
class UploadCmd(
    private val FileUploaderObj: FileUploader
): Callable<Int> {

    @CommandLine.Option(required = true, names=["-d","--dir"], description = ["Directory Path"])
    var dir: String? = null

    @CommandLine.Option(required = true, names=["-p","--process"], description = ["ProcessID"])
    var processID: String? = null

    @CommandLine.Option(required = false, names=["-o","--oid"], description = ["Observer ID"])
    var observerID:String? = null

    @CommandLine.Option(required = false, names=["-f"], description = ["JSON file path of metadata for the observation"])
    var metadata:String? = null


    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Utility for Test Commandline Options..."])
    var help = false

    override fun call(): Int  {

        when{
            help -> exitProcess(0)
            else -> {
//                var processID = "4935ff85-8e84-4b06-a69a-9ac160542a50" // TestSim/
//                var observerID = "d798e5be-344e-4e5e-994f-48d43e93d6d6"
//                val processID = "3d9adc35-e21e-43cc-b867-69b07305e75a"

                // createobservation
                //Here we first create the observationMetaData
//                val path = Paths.get("").toAbsolutePath()
//                val uploadDir = Paths.get("$path/upload/dat")
                var jsonFilePath: Path? =  metadata?.run {
                      when(Paths.get(metadata).isAbsolute){
                        false -> Paths.get(metadata).toAbsolutePath().normalize()
                        else -> Paths.get(metadata)
                    }
                }?:null

                val uploadDir = when(Paths.get(dir).isAbsolute){
                    false -> Paths.get(dir).toAbsolutePath().normalize()
                    else -> Paths.get(dir)
                }

                var oid: String = if (observerID==null){
                    // need to acquire the observerID
                    "d798e5be-344e-4e5e-994f-48d43e93d6d6"

                } else
                    observerID as String


                processID?.let {
                    println("$it ::  $oid  ::  $uploadDir :: $jsonFilePath")
                    FileUploaderObj.uploadDirectory(it, oid, uploadDir, jsonFilePath)
                }
            }
        }
        return 0
    }


}