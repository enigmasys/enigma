package command

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import java.util.concurrent.Callable

@Configuration
@Command(
    versionProvider = CLIVersionProvider::class,
    mixinStandardHelpOptions = true,
    description = ["Command for accessing the UDCP"],
)
@ComponentScan(basePackages = ["common", "common.util"])
class EnigmaCommand : Callable<Int> {
    @Option(
        names = ["-t", "--token"],
        description = ["Auth Token to pass when using Auth Passthrough Mode!"],
        scope = CommandLine.ScopeType.INHERIT,
    )
    var token: String? = null

    @CommandLine.Spec
    var spec: CommandSpec? = null

    override fun call(): Int {
        //! Print usage if no subcommand is given and exit with a non-zero status
        spec?.commandLine()?.usage(System.err)
        return 0
    }
}
