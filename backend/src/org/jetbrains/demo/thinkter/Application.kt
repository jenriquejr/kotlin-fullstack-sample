package org.jetbrains.demo.thinkter

import com.google.gson.*
import org.jetbrains.demo.thinkter.dao.*
import org.jetbrains.demo.thinkter.model.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.features.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.locations.*
import org.jetbrains.ktor.logging.*
import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.sessions.*
import org.jetbrains.ktor.transform.*

data class Session(val userId: String)

fun Application.main() {
    val storage = ThinkterDatabase(/*JDBCConnection.Companion.create(H2Dialect, pool)*/)

    install(DefaultHeaders)
    install(CallLogging)
    install(ConditionalHeaders)
    install(PartialContentSupport)
    install(Compression)
    install(Locations)
    install(StatusPages) {
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented) }
    }

    install(Routing) {
        withSessions<Session> {
            withCookieByValue {
                settings = SessionCookiesSettings(transformers = listOf(SessionCookieTransformerMessageAuthentication(hashKey)))
            }
        }

        transform.register<RpcData> {
            TextContent(Gson().toJson(it), ContentType.Application.Json)
        }

        index(storage)
        postThought(storage, ::hash)
        delete(storage, ::hash)
        userPage(storage)
        viewThought(storage, ::hash)

        login(storage, ::hash)
        register(storage, ::hash)

        get("/frontend/frontend.bundle.js") {
            call.respond(call.resolveClasspathWithPath("", "frontend.bundle.js")!!)
        }
    }
}

