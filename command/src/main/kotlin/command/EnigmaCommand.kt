package command


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.util.concurrent.Callable


import common.model.observation.UploadObservationObject
import common.model.testdata.MRIData
import common.util.prettyJsonPrint
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import picocli.CommandLine
import common.services.FileUploader
import common.services.ObservationServiceImpl
import common.services.ObservationUploadServiceImpl
import common.services.PremonitionProcessServiceImpl
import common.services.auth.AuthService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

//@Component
@Configuration
@Command(
    mixinStandardHelpOptions = true,
    description = ["Command for premonition datalake"],
)
@ComponentScan(basePackages = ["common","common.util"])
class EnigmaCommand(
) : Callable<Int> {
    @Option(names = ["-t", "--token"], description = ["Auth Token to pass when using Auth Passthrough Mode!"], scope = CommandLine.ScopeType.INHERIT)
    var token:String? = null
    override fun call(): Int {
        return 0
    }
}