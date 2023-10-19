package command

import common.services.TaxonomyInfoService
import common.services.auth.AuthService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import picocli.CommandLine
import picocli.CommandLine.Command
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.io.path.notExists
import kotlin.system.exitProcess

@Component
@Command(
    name = "download",
    aliases = ["pull"],
    mixinStandardHelpOptions = true,
)
@ComponentScan(basePackages = ["common","common.services.ObservationServiceImpl","common.services.PremonitionProcessServiceImpl","common.services.auth.AuthService"])
class DownloadCmdv1(
//    private val ObservationDownloadServiceObj: ObservationServiceImpl,
//    private val ProcessServiceObj: PremonitionProcessServiceImpl,
    private val taxonomyServiceObj: TaxonomyInfoService,
    private val AuthServiceObj: AuthService,
): Callable<Int>
{

    @CommandLine.Option(required = true, names=["-d","--dir"], description = ["Directory Path"])
    lateinit var dir: String

    @CommandLine.Option(required = false, names=["-p","--process", "-repo"],
        description = ["Repository ID (a.k.a. ProcessID) of the repository"])
    var processID: String? = null

    @CommandLine.Option(required = false, names=["-i","--index"], description = ["index of the record"])
    var obsIndex:String? = null

    @CommandLine.Option(required = false, names=["-e","--end"], description = ["end index of the record"])
    var endIndex:String? = null

    @CommandLine.Option(required = false, names=["-all"], description = ["Download all the records"])
    var allEntries:Boolean? = null

    @CommandLine.Option(required=false, names = ["-r", "--records"], split = ",")
    var record: IntArray? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true,
        description = ["Helps in downloading of records from a repository."])
    var help = false

    // We are deprecating the peek option for now
    //    @CommandLine.Option(required = false, names=["-m","--metadata"],
    //        description = ["Download ONLY all metadata appendFiles (without the data appendFiles)"])
    //    var peek = false

    @CommandLine.Option(required = false, names=["--uri"], description = ["URI of the Record to be Downloaded"])
    var uri: String? = null

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }


        when{
        help -> exitProcess(0)

         record?.isNotEmpty() == true ->{


             // make sure the processID is set else exit
             if (processID == null){
                 logger.info("Please provide a processID: $processID ")
                 exitProcess(0)
             }

             var version:String= 0.toString()


             // Make sure the string is a directory and not a file
            checkInputDirectory(dir)

//           taxonomyServiceObj.initTaxonomyInfoService(TAXONOMYPROJECT, TAXONOMYBRANCH)
           taxonomyServiceObj.initTaxonomyInfoService()
           var mapOfRepoIndexList = hashMapOf<String,ArrayList<String>>()

           // create a list of index to download from the start index to the end index
           mapOfRepoIndexList.putIfAbsent(processID!!, arrayListOf())
           version = 0.toString()


           record!!.forEach {
               mapOfRepoIndexList[processID]!!.add( it.toString()+"_"+version)
           }

           runBlocking {
//                val contentType = taxonomyServiceObj.getContentTypeOfRepository("87dc1607-5d63-4073-9424-720f86ecef43")
               val contentType = taxonomyServiceObj.getContentTypeOfRepository(processID!!)
               val tpath = taxonomyServiceObj.getPathofContentType(contentType)

               println("=====================================")
               println("Starting Download Operation")
               println("=====================================")
               println("contentType: $contentType")
               val differed = mapOfRepoIndexList.map { (repoId, indexList) ->
                   println("repoId: $repoId indexList: $indexList")
                   val tmp = indexList.joinToString(
                       prefix = "[",
                       postfix = "]",
                       separator = ","
                   ) { "\"$it\"" }
                   val deferred = async {
                       // Will this be thread safe?
//                        taxonomyServiceObj.downloadFile(repoId, tmp, tpath, "./newResult")
                       taxonomyServiceObj.downloadFile(repoId, tmp, tpath, dir)
                   }
                   deferred
               }
               differed.forEach { it.await() }
           }
       }

        uri?.isNotEmpty() == true -> {

            var version:String= 0.toString()
            // Make sure the string is a directory and not a file
            checkInputDirectory(dir)

            // parse the URI to be of the format: processID/index/version
            // given URI is pdp://leappremonitiondev.azurewebsites.net/vutest/ae0f62d0-854b-4696-8c7d-54e89e04308e/119/0
            // we need to extract the processID, index and version from the URI
            // processID = ae0f62d0-854b-4696-8c7d-54e89e04308e
            // index = 119
            // version = 0
            // the URI is of the format: pdp://leappremonitiondev.azurewebsites.net/vutest/ae0f62d0-854b-4696-8c7d-54e89e04308e/119/0

            // split the URI by "/"
            // First check that it is pdp.
            if (uri!!.split("/")[0] != "pdp:"){
                logger.info("Please provide a valid URI: $uri ")
                exitProcess(0)
            }
            val uriSplit = uri!!.split("/")
            //uriSplit array might look like this: [pdp:, , leappremonitiondev.azurewebsites.net, vutest, ae0f62d0-854b-4696-8c7d-54e89e04308e, 119, 0]
            // we need to extract the processID, index and version from the URI
            // processID = ae0f62d0-854b-4696-8c7d-54e89e04308e
            // index = 119
            // version = 0
            // the URI is of the format: pdp://leappremonitiondev.azurewebsites.net/vutest/ae0f62d0-854b-4696-8c7d-54e89e04308e/119/0
            val tmpProcessID = uriSplit[4]
            val index = uriSplit[5]
            var tmpVersion = uriSplit[6]


            taxonomyServiceObj.initTaxonomyInfoService()
            var mapOfRepoIndexList = hashMapOf<String,ArrayList<String>>()

            // create a list of index to download from the start index to the end index
            mapOfRepoIndexList.putIfAbsent(tmpProcessID, arrayListOf())
            mapOfRepoIndexList[tmpProcessID]!!.add( index+"_"+tmpVersion)
            runBlocking {
                val contentType = taxonomyServiceObj.getContentTypeOfRepository(tmpProcessID!!)
                val tpath = taxonomyServiceObj.getPathofContentType(contentType)

                println("=====================================")
                println("Starting Download Operation")
                println("=====================================")
                println("contentType: $contentType")
                val differed = mapOfRepoIndexList.map { (repoId, indexList) ->
                    println("repoId: $repoId indexList: $indexList")
                    val tmp = indexList.joinToString(
                        prefix = "[",
                        postfix = "]",
                        separator = ","
                    ) { "\"$it\"" }
                    val deferred = async {
                        val responseObj = taxonomyServiceObj.downloadFileUrls(repoId, tmp, tpath)
                        if (responseObj != null) {
                            taxonomyServiceObj.DownloadFiles(responseObj, dir)
                        }
                        taxonomyServiceObj.saveMetadataFile(repoId, indexList[0], tpath, dir)

                    }
                    deferred
                }
                differed.forEach { it.await() }
            }


            println("=====================================")
            println("Download Operation Completed")
            println("=====================================")



        }
        else ->{

            var startObsIndex:String? =  obsIndex
            var endObsIndex:String? = endIndex
            var version:String= 0.toString()

            // make sure the processID is set else exit
            if (processID == null){
                logger.info("Please provide a processID: $processID ")
                exitProcess(0)
            }

            checkInputDirectory(dir)




            if (startObsIndex == null){
                logger.info("Please provide either -i or -e or -all: $processID ")
                exitProcess(0)
            }

            if (endObsIndex == null){
                endObsIndex = startObsIndex
            }


            if (startObsIndex.toInt() < 0){
                logger.info("Start Index needs to be 0 or above: $processID ")
                exitProcess(0)
            }

            if (endObsIndex.toInt() < 0){
                logger.info("End Index needs to be 0 or above: $processID ")
                exitProcess(0)
            }

            if (startObsIndex.toInt() > endObsIndex.toInt()){
                logger.info("Start Index needs to be less than End Index: $processID ")
                exitProcess(0)
            }

//            taxonomyServiceObj.initTaxonomyInfoService(TAXONOMYPROJECT, TAXONOMYBRANCH)
            taxonomyServiceObj.initTaxonomyInfoService()
            var mapOfRepoIndexList = hashMapOf<String,ArrayList<String>>()

//            mapOfRepoIndexList["6e9da372-8cc7-4b11-bf85-23ed9d83a301"] = arrayListOf("54_0","55_0","53_0")

            // create a list of index to download from the start index to the end index
            mapOfRepoIndexList.putIfAbsent(processID!!, arrayListOf())
            version = 0.toString()

            for (i in startObsIndex.toInt()..endObsIndex.toInt()){
                mapOfRepoIndexList[processID]!!.add( i.toString()+"_"+version)
            }
//            mapOfRepoIndexList[processID] = arrayListOf(startObsIndex,endObsIndex)
            runBlocking {
                val contentType = taxonomyServiceObj.getContentTypeOfRepository(processID!!)
                val tpath = taxonomyServiceObj.getPathofContentType(contentType)

                println("=====================================")
                println("Starting Download Operation")
                println("=====================================")
                println("contentType: $contentType")
                val differed = mapOfRepoIndexList.map { (repoId, indexList) ->
                    println("repoId: $repoId indexList: $indexList")
                    val tmp = indexList.joinToString(
                        prefix = "[",
                        postfix = "]",
                        separator = ","
                    ) { "\"$it\"" }
                    val deferred = async {
                        val responseObj = taxonomyServiceObj.downloadFileUrls(repoId, tmp, tpath)
//
                        if (responseObj != null) {
                            taxonomyServiceObj.DownloadFiles(responseObj, dir)
                        }

                            taxonomyServiceObj.saveMetadataFile(repoId, indexList[0], tpath, dir)
                    }
                    deferred
                }
                differed.forEach { it.await() }
            }




            println("=====================================")
            println("Download Operation Completed")
            println("=====================================")
        }
    }
        return 0

    }

    private fun checkInputDirectory(dir: String) {
        println("dir: ${this.dir}")
        Files.isDirectory(Paths.get(this.dir)).let {
            if (!it) {
                println("Make sure to Provide Valid Directory Path(NOT a File) for: ${this.dir}")
                println("Please provide a directory and try again.")
                exitProcess(0)
            }
        }
        // Make sure the directory exists
        this.dir?.let {
            if (Paths.get(this.dir).notExists()) {
                println("Directory does not exist: ${this.dir}")
                println("Please create the directory and try again.")
                exitProcess(0)
            }
        }
    }

}


