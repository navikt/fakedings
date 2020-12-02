package fakedings

import mu.KotlinLogging
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.OAuth2Config
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer

private val log = KotlinLogging.logger { }

fun main() {

    val mockOAuth2Server = MockOAuth2Server(
        OAuth2Config(
            interactiveLogin = false
        )
    )
    mockOAuth2Server.start(port = 1111)
    val rsaKey = createRSAKey("generated")
    routes(
        "token/client_assertion" bind Method.GET to { req ->

            val clientId: String = req.query("client_id") ?: "notfound"

            val jwt = clientAssertion(
                clientId,
                mockOAuth2Server.tokenEndpointUrl("tokenx").toString(),
                rsaKey
            )

            Response(Status.OK).body(jwt.serialize())
        },
        "/token/idporten" bind Method.GET to { req ->
            val pid: String = req.query("pid") ?: "notfound"
            val acr: String = req.query("acr") ?: "Level4"
            val token = mockOAuth2Server.issueIdportenToken(pid, acr)
            Response(Status.OK).body(token.serialize())
        },
        "/token/aad" bind Method.GET to { req ->
            val preferredUsername: String = req.query("preferred_username") ?: "notfound"
            val name: String = req.query("name") ?: "notfound"
            val token = mockOAuth2Server.issueAADToken(preferredUsername, name)
            Response(Status.OK).body(token.serialize())
        }
    ).asServer(Netty(8080)).start()
}
