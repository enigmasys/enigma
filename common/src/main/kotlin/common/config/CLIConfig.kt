package common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "cliclient")
class CLIConfig {
    lateinit var taxonomyServiceUrl: String
    lateinit var taxonomyProject: String
    lateinit var taxonomyBranch: String
}


