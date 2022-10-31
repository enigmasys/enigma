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
import java.time.LocalDate
import kotlin.random.Random
import io.github.serpro69.kfaker.Faker


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

    @CommandLine.Option(required = true, names=["-p","--process"], description = ["ProcessID"])
    var processID: String? = null

    @CommandLine.Option(required = false, names=["-o","--oid"], description = ["Observer ID"])
    var observerID:String? = null

    @CommandLine.Option(required = false, names=["-f"], description = ["JSON file path of metadata for the observation"])
    var metadata:String? = null


    @CommandLine.Option(names = ["-h", "--help"], usageHelp = true, description = ["Utility for Test Commandline Options..."])
    var help = false

    @CommandLine.ParentCommand
    val parent: EnigmaCommand? = null


    @CommandLine.ArgGroup(exclusive = false,multiplicity = "0..1")
    var dependent: Dependent? = null


    @CommandLine.Option(names = ["-synth"], required = false, description = ["synthesize Fake Data"])
    var populateFakeData: Boolean = false

    class Dependent {
        @CommandLine.Option(names = ["-validate"], required = true)
        var validate:Boolean = false

        @CommandLine.Option(names = ["-type"], required = true, description = ["Taxonomy input source can be either of type either url or file"])
        var type = ""
        @CommandLine.Option(names = ["-path"], required = true, description = ["Taxonomy input source path - (file path/ URL address)"])
        var path = ""



    }

    fun generateDataProcessProfileObs(): String{
        var dataprocessProfile = """
            {
             "displayName" : "MSSM_SANDBOX_DATASET_1",
             "taxonomyTags" :[],
             "profile":{"Creator": "Yogesh B.", "Created":"10-25-2022"}
             }
            }
        """.trimIndent()
        return dataprocessProfile
    }
    fun generateEntry(): String {

        val faker = Faker()
        var investigator_id = faker.barcode.compositeSymbol()
        var subject_gender = faker.gender.binaryTypes()
        var subject_birthdate = faker.person.birthDate(age = 30, at = LocalDate.of(2022, 1, 1)) // => 1990-06-15
        var subject_enddate = faker.person.birthDate(age = 1, at = LocalDate.of(2022, 1, 1)) // => 1990-06-15
        var subject_startdate = faker.person.birthDate(age = 2, at = LocalDate.of(2022, 1, 1)) // => 1990-06-15
        var subject_location = faker.address.cityWithState()

//    println("Hello $investigator_id")

        var height = Random.nextInt(from = 5, until = 7).toString()
        var weights = Random.nextInt(from=100, until = 240).toString()
//    println("height $height")

    var releasev1aData =
      """  
     {
    "displayName" : "MSSM_SANDBOX_DATASET_1",
    "taxonomyTags" :[
                {
                    "c26ff14b-302b-8824-88b3-2b8314dc315b": {
                        "124ad295-6f66-fe85-7921-b6ba587e6dd6": "Icahn School of Medicine at Mount Sinai",
                        "ID": "c26ff14b-302b-8824-88b3-2b8314dc315b"
                    },
                    "ae99737b-2590-2bd6-1533-f6add5407946": "Main Campus",
                    "ID": "fb235ea0-8c4c-63ff-30ff-0c4e4762bde5"
                },
                {
                    "012eb816-0ac8-008f-eb89-a96b59443a03": "Michael R.",
                    "ID": "1ae95799-911c-5365-d928-78dd7e363546"
                },
                {
                    "aebba08c-d629-2bc5-3e46-225e1430c9da": "AALTO_SANDBOX_1",
                    "ID": "70c5eddd-5efa-d23f-8c1b-424b1c9ed350"
                },
                {
                    "70c5eddd-5efa-d23f-8c1b-424b1c9ed350": {
                        "aebba08c-d629-2bc5-3e46-225e1430c9da": "AALTO_SANDBOX_1",
                        "ID": "70c5eddd-5efa-d23f-8c1b-424b1c9ed350"
                    },
                    "d495dff7-5c98-e1c4-5090-df6be1b8a48d": "DP Sleep",
                    "ID": "06414fb1-fcc8-06eb-4ed1-1ca45a2b5de4"
                }
            ]}
        """.trimIndent()



        var newdummyData =
"""
    {
    "displayName" : "MSSM_SANDBOX_DATASET_1",
    "taxonomyTags" :
    [{"47c67f13-47b2-6c35-097f-9458d53614a5":"$subject_enddate",
    "8d56c5a3-3d81-fbdc-2890-55330744d344":"$subject_startdate",
    "6df83c75-e4b1-3174-3153-af11d7f8c6a0":"1 month",
    "ID":"589ba005-8522-7e4a-3e66-215e9b9b74b5"},
    {"26f6428a-b724-e601-0f4e-d636111948e2":
    {"ID":"26f6428a-b724-e601-0f4e-d636111948e2"},
    "06b13b3b-34fc-7d0b-92d6-144d5f3e0741":"dpsleep",
    "03dbb70a-25bb-30a7-5a44-730c1895ef1a":"GeneActiv",
    "ID":"73c620ac-fdd0-e67b-6b9d-688f11c4bf07"},
    {"1dbcf8ca-0bf8-a6f4-6573-d695d30d1e0d":"AB12C",
    "d9a3e3b3-3a27-ea69-60bc-98a9c892cc35":"VUMC",
    "13991312-052a-9b0b-f6e7-dd2ded09a111":"$investigator_id",
    "ID":"91817e29-792d-a023-e541-bd931fcfcb0b"},
    {
    "24f97d53-eec9-8430-c904-011a8514b95f":"$height",
    "7213bd9d-e20d-fdbb-9374-a97568fc89bd":"$subject_gender",
    "877c8cca-26bb-8522-1aad-ce6346ef29ac":"$subject_birthdate",
    "1c260390-c770-32d8-1693-8a5cbfb42cfb":"Right",
    "6ab19632-217f-9d92-c7f0-c1487fcad205":"$weights",
    "27c62295-00b8-c098-e500-3c5bde60f8fe":
    "$subject_location",
    "ID":"8a2a08b6-385d-d838-f6ba-5b616cdb8fff"}]
    }
""".trimIndent()
//
//        var dummyData = """
//    {
//            "taxonomyTags": [
//            {
//                "tagName": "Collection Time",
//                "End DateTime": "$subject_enddate",
//                "Start DateTime": "$subject_startdate",
//                "Frequency": "1 month",
//                "Tag": "Collection Time"
//            },
//            {
//                "tagName": "Actigraphy",
//                "Data Type": {
//                    "Tag": "Data Type"
//                    },
//                "Processing Pipeline": "dpsleep",
//                "Device": "GENEActiv",
//                "Tag": "Actigraphy"
//            },
//            {
//                "tagName": "Study",
//                "Code": "AB12C",
//                "Center": "Vanderbilt",
//                "Investigator ID": "$investigator_id",
//                "Tag": "Study"
//            },
//            {
//                "tagName": "Subject",
//                "Height": "$height",
//                "Sex": "$subject_gender",
//                "Date of Birth": "$subject_birthdate",
//                "Handedness": "Right",
//                "Weight": "$weights",
//                "Location": "$subject_location",
//                "Tag": "Subject"
//            }
//            ]
//        }
//""".trimIndent()
//        return newdummyData
        return releasev1aData
    }


    override fun call(): Int  {
        parent?.let { it ->
            if (it.token?.length?.compareTo(0) ?:  0  > 0){
                parent?.token?.let { it -> AuthServiceObj.setAuthToken(it) }
            } }

        when{
            help -> exitProcess(0)


            populateFakeData ->{

                var jsonFilePath: Path? =  metadata?.run {
                    when(Paths.get(metadata).isAbsolute){
                        false -> Paths.get(metadata).toAbsolutePath().normalize()
                        else -> Paths.get(metadata)
                    }
                }?:null

                dependent?.validate?.let {
                    println("Validate Flags is set to ${dependent?.validate}")
                    println("JSONSchema Path Type: ${dependent?.type}")
                    println("JSONSCHEMA Path: ${dependent?.path}")
                    var validationResult:Boolean = false
                    when(dependent?.type){
                        "file" -> {
                            var jsonSchemaFilePath: Path? =  dependent?.path?.run {
                                when(Paths.get(dependent?.path).isAbsolute){
                                    false -> Paths.get(dependent?.path).toAbsolutePath().normalize()
                                    else -> Paths.get(dependent?.path)
                                }
                            }?:null
                            validationResult = JSONSchemaValidatorObj.validate(
                                ResourceSourceType.FILE,
                                jsonSchemaFilePath.toString(),
                                jsonFilePath.toString()
                            )
                        }
                        "url"->{
                            validationResult = JSONSchemaValidatorObj.validate(
                                ResourceSourceType.URL,
                                dependent?.path.toString(),
                                jsonFilePath.toString()
                            )
                        }
                    }
                    if (!validationResult)
                        exitProcess(0)
                }

                val uploadDir = Paths.get(dir?.let { tryExtendPath(it) })
//                val uploadDir = when(Paths.get(dir).isAbsolute){
//                    false ->
//                        Paths.get(dir).toAbsolutePath().normalize()
//                    else -> Paths.get(dir)
//                }

                var oid: String = if (observerID==null){
                    // need to acquire the observerID
//                    "d798e5be-344e-4e5e-994f-48d43e93d6d6"
                    UserInfoObj.getUserRegistration()!!.userId

                } else
                    observerID as String


                repeat(1){
                    var dummydata = generateDataProcessProfileObs()

//                    var dummydata = generateEntry()


                    processID?.let {
                        logger.info("$it ::  $oid  ::  $uploadDir :: $jsonFilePath")
                        FileUploaderObj.uploadDirectoryNew(it, oid, uploadDir, dataJSON = dummydata )
                    }
                }
                repeat(1){
//                    var dummydata = generateDataProcessProfileObs()

                    var dummydata = generateEntry()


                    processID?.let {
                        logger.info("$it ::  $oid  ::  $uploadDir :: $jsonFilePath")
                        FileUploaderObj.uploadDirectoryNew(it, oid, uploadDir, dataJSON = dummydata )
                    }
                }

            }
            else -> {
//
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

                dependent?.validate?.let {
                    println("Validate Flags is set to ${dependent?.validate}")
                    println("JSONSchema Path Type: ${dependent?.type}")
                    println("JSONSCHEMA Path: ${dependent?.path}")
                    var validationResult:Boolean = false
                    when(dependent?.type){
                        "file" -> {
                            var jsonSchemaFilePath: Path? =  dependent?.path?.run {
                                when(Paths.get(dependent?.path).isAbsolute){
                                    false -> Paths.get(dependent?.path).toAbsolutePath().normalize()
                                    else -> Paths.get(dependent?.path)
                                }
                            }?:null
                             validationResult = JSONSchemaValidatorObj.validate(
                                ResourceSourceType.FILE,
                                jsonSchemaFilePath.toString(),
                                jsonFilePath.toString()
                            )
                        }
                        "url"->{
                            validationResult = JSONSchemaValidatorObj.validate(
                                ResourceSourceType.URL,
                                dependent?.path.toString(),
                                jsonFilePath.toString()
                            )
                        }
                    }
                    if (!validationResult)
                        exitProcess(0)
                }

                val uploadDir = Paths.get(dir?.let { tryExtendPath(it) })
//                val uploadDir = when(Paths.get(dir).isAbsolute){
//                    false ->
//                        Paths.get(dir).toAbsolutePath().normalize()
//                    else -> Paths.get(dir)
//                }

                var oid: String = if (observerID==null){
                    // need to acquire the observerID
//                    "d798e5be-344e-4e5e-994f-48d43e93d6d6"
                    UserInfoObj.getUserRegistration()!!.userId

                } else
                    observerID as String


                processID?.let {
                    logger.info("$it ::  $oid  ::  $uploadDir :: $jsonFilePath")
                    FileUploaderObj.uploadDirectory(it, oid, uploadDir, jsonFilePath)
                }
            }
        }


        return 0
    }


}