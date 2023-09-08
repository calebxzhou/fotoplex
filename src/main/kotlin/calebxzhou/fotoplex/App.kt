package calebxzhou.fotoplex

import calebxzhou.fotoplex.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

val IMAGE_DIR = System.getProperty("user.dir").let { File(it,"images") }
fun main() {
    IMAGE_DIR.mkdir()
    embeddedServer(Netty, port = 9999, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}
