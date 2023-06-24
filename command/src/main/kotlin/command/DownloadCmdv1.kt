package command

import common.config.LogFilter
import common.services.*
import common.services.auth.AuthService
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import picocli.CommandLine
import picocli.CommandLine.Command
import reactor.netty.http.client.HttpClient
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

    @CommandLine.Option(required = true, names=["-p","--process", "-repo"], description = ["Repository ID (a.k.a. ProcessID) of the repository"])
    lateinit var processID: String

//    @CommandLine.Option(required = false, names=["-o","--oid"], description = ["Observer ID"])
//    var observerID:String? = null

    @CommandLine.Option(required = false, names=["-i","--index"], description = ["index of the record"])
    var obsIndex:String? = null


    @CommandLine.Option(required = false, names=["-e","--end"], description = ["end index of the record"])
    var endIndex:String? = null

    @CommandLine.Option(required = false, names=["-all"], description = ["Download all the records"])
    var allEntries:Boolean? = null

    @CommandLine.Option(required=false, names = ["-r", "--records"], split = ",")
    var record: IntArray? = null

    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Helps in downloading of records from a repository."])
    var help = false

    @CommandLine.Option(required = false, names=["-m","--metadata"], description = ["Download ONLY all metadata appendFiles (without the data appendFiles)"])
    var peek = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null

    val logger = LoggerFactory.getLogger(this::class.java)


    @Value("\${cliclient.taxonomyProject}")
    private val TAXONOMYPROJECT: String = "AllLeap+TaxonomyBootcamp123"
    @Value("\${cliclient.taxonomyBranch}")
    private val TAXONOMYBRANCH: String = "master123"

    override fun call(): Int {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }


        when{
        help -> exitProcess(0)

         record?.isNotEmpty() == true ->{

             var version:String= 0.toString()


           taxonomyServiceObj.initTaxonomyInfoService(TAXONOMYPROJECT, TAXONOMYBRANCH)
           var mapOfRepoIndexList = hashMapOf<String,ArrayList<String>>()

//            mapOfRepoIndexList["6e9da372-8cc7-4b11-bf85-23ed9d83a301"] = arrayListOf("54_0","55_0","53_0")

           // create a list of index to download from the start index to the end index
           mapOfRepoIndexList.putIfAbsent(processID, arrayListOf())
           version = 0.toString()


           record!!.forEach {
               mapOfRepoIndexList[processID]!!.add( it.toString()+"_"+version)
           }

           runBlocking {
//                val contentType = taxonomyServiceObj.getContentTypeOfRepository("87dc1607-5d63-4073-9424-720f86ecef43")
               val contentType = taxonomyServiceObj.getContentTypeOfRepository(processID)
               val tpath = taxonomyServiceObj.getPathofContentType(contentType)
               println("contentType: $contentType tpath: $tpath")
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

        else ->{

            var startObsIndex:String? =  obsIndex
            var endObsIndex:String? = endIndex
            var version:String= 0.toString()



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

            taxonomyServiceObj.initTaxonomyInfoService(TAXONOMYPROJECT, TAXONOMYBRANCH)
            var mapOfRepoIndexList = hashMapOf<String,ArrayList<String>>()

//            mapOfRepoIndexList["6e9da372-8cc7-4b11-bf85-23ed9d83a301"] = arrayListOf("54_0","55_0","53_0")

            // create a list of index to download from the start index to the end index
            mapOfRepoIndexList.putIfAbsent(processID, arrayListOf())
            version = 0.toString()

            for (i in startObsIndex.toInt()..endObsIndex.toInt()){
                mapOfRepoIndexList[processID]!!.add( i.toString()+"_"+version)
            }
//            mapOfRepoIndexList[processID] = arrayListOf(startObsIndex,endObsIndex)
            runBlocking {
//                val contentType = taxonomyServiceObj.getContentTypeOfRepository("87dc1607-5d63-4073-9424-720f86ecef43")
                val contentType = taxonomyServiceObj.getContentTypeOfRepository(processID)
                val tpath = taxonomyServiceObj.getPathofContentType(contentType)
                println("contentType: $contentType tpath: $tpath")
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




            println("=====================================")
            println("Download Operation Completed")
            println("=====================================")
        }
    }
        return 0

    }


    fun getWebGMEToken(): String? {
        val token = AuthServiceObj.getAuthToken()
        println(token)
//        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        //create webclient and enable follow redirect
        var webClient = WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().followRedirect(true)
                )
            )
            .filters { exchangeFilterFunctions ->
                exchangeFilterFunctions.add(LogFilter.logRequest())
                exchangeFilterFunctions.add(LogFilter.logResponse())
            }
            .baseUrl("https://wellcomewebgme.centralus.cloudapp.azure.com")
            .build()
        // Here we fetch the token from the webgme Server
        val response = webClient.get()
            .uri("/aad/device")
            .headers { it.setBearerAuth(token) }
            .accept(MediaType.ALL)
            .exchange().block()
//            .retrieve()
//            .bodyToMono(String::class.java)
//            .block()
//            println(response)
        val access_token = response?.cookies()?.get("access_token")?.get(0)?.value
        return access_token

    }

    private fun downloadFiles(
        dir: String,
        index: String,
        version: String = "0",
        processID: String,
        tokenObj: Map<String, String?>,
    ){
        val downloadDir = when (Paths.get(dir).isAbsolute) {
            false -> Paths.get(dir).toAbsolutePath().normalize()
            else -> Paths.get(dir)
        }

        val accessToken = tokenObj["webgmeToken"]
        if (downloadDir.notExists()){
            println("Directory does not exist: $downloadDir")
            println("Please create the directory and try again.")
            exitProcess(0)
//            Files.createDirectories(downloadDir)
        }


//        var webClient = WebClient.builder()
//            .clientConnector(
//                ReactorClientHttpConnector(
//                    HttpClient.create().followRedirect(true)
//                )
//            )
//            .filters { exchangeFilterFunctions ->
//                exchangeFilterFunctions.add(LogFilter.logRequest())
//                exchangeFilterFunctions.add(LogFilter.logResponse())
//            }
//            .baseUrl("https://wellcomewebgme.centralus.cloudapp.azure.com")
//            .build()

        val token = tokenObj["aadToken"]


//        val encodedProjectID = "AllLeap+TaxonomyBootcamp"
//        val encodedProjectBranch = "master"
        var finalCookie = "webgme_aad=$token; access_token=$accessToken ;"
        val downloadURL = taxonomyServiceObj.getDownloadURL(processID, "[${index}_${version}]")
        println(downloadURL)
        exitProcess(0)

        //  https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Dashboard/AllLeap%2BTaxonomyBootcamp/branch/master/static/index.html

        // construct download URL for the ProcessID with the index

//        fun getDownloadURL(processID: String, index: String): String {
//            val encodedProjectID = "AllLeap+TaxonomyBootcamp"
//            val encodedProjectBranch = "master"
//            val contentTypeRef = getProcessIDinfo(processID)
////            val downloadURL = "https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Dashboard/$encodedProjectID/branch/$encodedProjectBranch/$contentTypeRef/$processID/download?ids=$index"
//            val downloadURL = "/routers/Dashboard/$encodedProjectID/branch/$encodedProjectBranch/$contentTypeRef/$processID/download?ids=$index"
//            return downloadURL
//        }


//        var response = webClient.get()
//            .uri { uriBuilder: UriBuilder ->
//                UriComponentsBuilder.fromUri(uriBuilder.build())
////                .path("routers/TagFormat/$encodedProjectID/branch/$encodedProjectBranch/human")
//                    .path("routers/Dashboard/{encodedProjectID}/branch/{encodedProjectBranch}//t/{processID}/download?ids={index}")
//                    .encode()
//                    .buildAndExpand(encodedProjectID, encodedProjectBranch, processID,index)
////                .buildAndExpand(guidTagsEncoded)
//                    .toUri()
//            }
//            .header(HttpHeaders.COOKIE, finalCookie)
//            .retrieve()
//            .bodyToMono(ByteArray::class.java)
//            .block()

//        println("=====================================")

    //        https://wellcomewebgme.centralus.cloudapp.azure.com/routers/Search
    //        /AllLeap+TaxonomyBootcamp
    //        /branch/master
    //        //t
    //        /artifacts
    //        /ae0f62d0-854b-4696-8c7d-54e89e04308e
    //        /download
    //        ?
    //        ids=["13_0"]

        }
    }

//    private fun saveMetadata(resultData: UploadObservationObject?, ObsIndex: String?,version:String="0") {
//        resultData?.let {
//    //                prettyJsonPrint(it)
//            val mapper = ObjectMapper()
//            val downloadDir = when (Paths.get(dir).isAbsolute) {
//                false -> Paths.get(dir).toAbsolutePath().normalize()
//                else -> Paths.get(dir)
//            }
////            val nFile = "$downloadDir/metadata/$startObsIndex/metadata.json"
//            val nFile = "$downloadDir/$ObsIndex/$version/metadata.json"
//            var tmpTags = mapper.convertValue(it.data, Array<TaxonomyData>::class.java)[0].taxonomyTags
//            val processInfo = ProcessServiceObj.getProcessState(processID)
//            var index0_data = ObservationDownloadServiceObj.getObservation(
//                processID = processID,
//                startObsIndex = 0.toString(), // We always download from the 0th index.
//                version = processInfo?.lastVersionIndex.toString()
//            )
//            val tmpTaxonomyData = mapper.convertValue(index0_data!!.data, Array<TaxonomyData>::class.java)
////            val displayName = tmpTaxonomyData[0].displayName
//            // This will be the tagFormatter URL to be called for the given taxonomy data.
//            var tmp_baseurl = ""
//            if (!tmpTaxonomyData[0].taxonomyVersion!!.url?.contains("http")!!) {
//                tmp_baseurl = "http://" + tmpTaxonomyData[0].taxonomyVersion!!.url
//            } else
//                tmp_baseurl = tmpTaxonomyData[0].taxonomyVersion!!.url.toString()
//
//
//            if (tmpTags?.size!! > 0) {
//                val tmpdata = tmpTaxonomyData[0].taxonomyVersion!!.branch?.let { it1 ->
//                    tmpTaxonomyData[0].taxonomyVersion!!.id?.let { it2 ->
//                        TaxonomyServerClient().getHumanTags(
//                            tmp_baseurl, it2,
//                            it1, mapper.writeValueAsString(tmpTags)
//                        )
//                    }
//                }
//                if (tmpdata != null) {
//                    tmpTaxonomyData[0].taxonomyTags = mapper.readValue(tmpdata)
//                    //                    (it.data?.get(0) as TaxonomyData).taxonomyTags = ObjectMapper().readTree(tmpdata).toList()
//    //                    (it.data?.get(0) as TaxonomyData).taxonomyTags = mapper.readValue(tmpdata!!)
//                }
//            }
//
//            it.data = tmpTaxonomyData.toList()
//            val file = AppendFile(nFile)
//            val tmpDir = Paths.get(nFile).parent
//
//            if (Files.notExists(tmpDir))
//                Files.createDirectories(tmpDir)
//            println("Saving metadata to ${file.absolutePath}")
//            file.createNewFile()
//            mapper.writeValue(file, (it.data as List<*>)[0])
//        }
//    }
