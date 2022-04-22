package edu.vanderbilt.enigma
import edu.vanderbilt.enigma.command.EnigmaCommand
import edu.vanderbilt.enigma.command.ProcessCmd
import edu.vanderbilt.enigma.command.UploadCmd
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import picocli.CommandLine


@SpringBootApplication
class EnigmaApplication
(
	private val generateEnigmaCommand: EnigmaCommand,
	private val generateUploadCmd: UploadCmd,
	private val generateProcessCmd: ProcessCmd

): CommandLineRunner
{
	override fun run(vararg args: String?) {
		CommandLine(generateEnigmaCommand)
			.addSubcommand(generateUploadCmd)
			.addSubcommand(generateProcessCmd)
			.execute(*args)
	}
}

fun main(args: Array<String>) {
	runApplication<EnigmaApplication>(*args)
}
