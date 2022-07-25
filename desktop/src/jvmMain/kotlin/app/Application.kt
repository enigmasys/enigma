package app

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import common.services.*
import common.model.*
import common.config.*

@SpringBootApplication
@ComponentScan("common")
class Application : CommandLineRunner {
    override fun run(vararg args: String?) {}
}