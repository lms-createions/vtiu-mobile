package com.example.vtiu.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.example.vtiu.server.db.DatabaseFactory
import com.example.vtiu.server.routes.*

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Initialize Database in the background to prevent blocking server startup
    launch {
        try {
            DatabaseFactory.init()
        } catch (e: Exception) {
            log.error("Failed to initialize database: ${e.message}")
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Unknown error")))
        }
    }
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
    }

    routing {
        get("/") {
            call.respondText("VTIU Server version 2.0 (Debugging 400)")
        }
        
        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }

        authRoutes()
        studentRoutes()
        teacherRoutes()
        vClassRoutes()
        financeRoutes()
        appointmentRoutes()
    }
}
