package com.example.vtiu.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.callloging.*
import com.example.vtiu.server.db.DatabaseFactory

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        anyHost()
    }
    install(CallLogging)

    routing {
        get("/") {
            call.respondText("VTIU Server is running!")
        }
        
        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }

        // Add your LMS API routes here
        route("/api") {
            get("/test") {
                call.respond(mapOf("message" to "API is working"))
            }
        }
    }
}
