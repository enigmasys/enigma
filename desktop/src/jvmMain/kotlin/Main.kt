// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.App
import app.Application
import common.services.PremonitionProcessServiceImpl
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

val context by lazy { _context }
private lateinit var _context: ApplicationContext







fun main() = application {

    _context = SpringApplication(Application::class.java).apply {
        webApplicationType = WebApplicationType.NONE
    }.run()


//    _context = SpringApplication(Application::class.java).run()

    val processService = _context.getBean(PremonitionProcessServiceImpl::class.java)

    Window(
        onCloseRequest = {
            SpringApplication.exit(context)
            exitApplication()
        },
        title = "Design Studio"
    ) {
        (_context as ConfigurableApplicationContext?)?.let { App(it) }
    }
}
