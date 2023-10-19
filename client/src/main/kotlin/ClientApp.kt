package edu.vanderbilt.enigma.client

import command.*
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import picocli.CommandLine

@SpringBootApplication
@ComponentScan(basePackages = ["command", "common"])
class ClientApp
(
        private val generateEnigmaCommand: EnigmaCommand,
        private val generateProcessCmd: ProcessCmdv1,
        private val generateDownloadCmdv1: DownloadCmdv1,
        private val generateUploadCmdv1: UploadCmdv1,
        private val generateUserComd: UserInfoCmd
        //	private val generateDevCmd: DevCmd
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val subcommands = CommandLine(generateEnigmaCommand)
                .addSubcommand(generateProcessCmd)
                .addSubcommand(generateDownloadCmdv1)
                .addSubcommand(generateUploadCmdv1)
                .addSubcommand(generateUserComd)

        subcommands.execute(*args)
    }
}

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(ClientApp::class.java.name)
    try {
        runApplication<ClientApp>(*args)
    } catch (e : Exception) {
        logger.error(e.toString())
    }
}
