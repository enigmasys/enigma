package edu.vanderbilt.enigma.secretapp
import command.DownloadCmd
import command.EnigmaCommand
import command.ProcessCmd
import command.UploadCmd
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import picocli.CommandLine


@SpringBootApplication
@ComponentScan(basePackages = ["command","common"])
class EnigmaApplication
(
	private val generateEnigmaCommand: EnigmaCommand,
	private val generateUploadCmd: UploadCmd,
	private val generateProcessCmd: ProcessCmd,
	private val generateDownloadCmd: DownloadCmd

): CommandLineRunner
{
	override fun run(vararg args: String?) {
		CommandLine(generateEnigmaCommand)
			.addSubcommand(generateUploadCmd)
			.addSubcommand(generateProcessCmd)
			.addSubcommand(generateDownloadCmd)
			.execute(*args)
	}
}

fun main(args: Array<String>) {
	runApplication<EnigmaApplication>(*args)
}
