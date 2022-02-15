package edu.vanderbilt.enigma.command

import org.springframework.stereotype.Component
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.time.LocalDate
import java.util.concurrent.Callable

@Component
@Command(
    name = "premcli",
    mixinStandardHelpOptions = true,
    version = ["premcli"],
    description = ["Command for premonition datalake"]
)
class EnigmaCommand(
//    val generatorService: GeneratorService,
) : Callable<Int> {
    @Option(required = true, names = ["-ops", "--invoice-number"], description = ["Operation to Work on"])
    private var invoiceNumber: String = ""

    @Option(names = ["-WDS", "--work-date-start"], description = ["Work date start"])
    private var workDateStart: LocalDate = LocalDate.now()

    @Option(names = ["-WDE", "--work-date-end"], description = ["Work date end"])
    private var workDateEnd: LocalDate = LocalDate.now()

    @Option(names = ["-H", "--hours"], description = ["Hours"])
    private var hours: Int = 0

    override fun call(): Int {
//        generatorService.generate(
//            ArgsDto(
//                invoiceNumber = invoiceNumber,
//                workDateStart = workDateStart,
//                workDateEnd = workDateEnd,
//                hours = hours
//            )
//        )
        return 0
    }
}