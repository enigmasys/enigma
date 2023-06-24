package edu.vanderbilt.enigma.client
import command.EnigmaCommand
import command.DownloadCmdv1
import command.ProcessCmdv1
import command.UploadCmdv1
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import picocli.CommandLine

@SpringBootApplication
@ComponentScan(basePackages = ["command","common"])
class ClientApp
    (
    private val generateEnigmaCommand: EnigmaCommand,
    private val generateProcessCmd: ProcessCmdv1,
    private val generateDownloadCmdv1: DownloadCmdv1,
    private val generateUploadCmdv1: UploadCmdv1

//	private val generateDevCmd: DevCmd

): CommandLineRunner
{
    override fun run(vararg args: String?) {
        CommandLine(generateEnigmaCommand)
            .addSubcommand(generateProcessCmd)
            .addSubcommand(generateDownloadCmdv1)
            .addSubcommand(generateUploadCmdv1)
            .execute(*args)
    }
}

fun main(args: Array<String>) {
    runApplication<ClientApp>(*args)
}
