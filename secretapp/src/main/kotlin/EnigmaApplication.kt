package edu.vanderbilt.enigma.secretapp
import command.*
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
	private val generateDownloadCmd: DownloadCmd,
	private val generateUserStatusCmd: UserInfoCmd,
		private val generateAdminCmd: AdminCmd
//	private val generateDevCmd: DevCmd

): CommandLineRunner
{
	override fun run(vararg args: String?) {
		CommandLine(generateEnigmaCommand)
			.addSubcommand(generateUploadCmd)
			.addSubcommand(generateProcessCmd)
			.addSubcommand(generateDownloadCmd)
			.addSubcommand(generateUserStatusCmd)
			.addSubcommand(generateAdminCmd)
//			.addSubcommand(generateDevCmd)
			.execute(*args)
	}
}

fun main(args: Array<String>) {
	runApplication<EnigmaApplication>(*args)
}
