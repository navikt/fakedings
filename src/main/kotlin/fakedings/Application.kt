package fakedings

import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Instant
import java.util.Date
import java.util.UUID
import mu.KotlinLogging
import no.nav.security.mock.oauth2.MockOAuth2Dispatcher
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.extensions.asOAuth2HttpRequest
import no.nav.security.mock.oauth2.extensions.toIssuerUrl
import okhttp3.Headers
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

private val log = KotlinLogging.logger { }

typealias PathDispather = (RecordedRequest) -> MockResponse

val rsaKey = createRSAKey("clientAssertionKey")

fun main() {
    val mockOAuth2Server = MockOAuth2Server()

    val pathDispatchers: Map<String, PathDispather> = mapOf(
        "/internal/isalive" to {
            ok("alive")
        },
        "/fake/client_assertion" to {
            val clientId: String = it.param("client_id") ?: "notfound"
            val audience: String = it.param("aud") ?: "notfound"
            val jwt = clientAssertion(clientId, audience, rsaKey)
            ok(jwt.serialize())
        },
        "/fake/idporten" to {
            val req = it.asOAuth2HttpRequest()
            val pid: String = it.param("pid") ?: "notfound"
            val acr: String = it.param("acr") ?: "Level4"
            val token = mockOAuth2Server.anyToken(
                req.url.toIssuerUrl(),
                mapOf(
                    "sub" to UUID.randomUUID().toString(),
                    "aud" to "notfound",
                    "at_hash" to UUID.randomUUID().toString(),
                    "amr" to listOf("BankId"),
                    "pid" to pid,
                    "locale" to "nb",
                    "acr" to acr,
                    "sid" to UUID.randomUUID().toString(),
                    "auth_time" to Date.from(Instant.now())
                )
            )
            ok(token.serialize())
        },
        "/fake/aad" to {
            val req = it.asOAuth2HttpRequest()
            val preferredUsername: String = it.param("preferred_username") ?: "notfound"
            val name: String = it.param("name") ?: "notfound"
            val token = mockOAuth2Server.anyToken(
                req.url.toIssuerUrl(),
                mapOf(
                    "sub" to UUID.randomUUID().toString(),
                    "aud" to "notfound",
                    "aio" to UUID.randomUUID().toString(),
                    "azpacr" to "1",
                    "azp" to "client id på den som spør",
                    "name" to name,
                    "oid" to UUID.randomUUID().toString(),
                    "preferred_username" to preferredUsername,
                    "scp" to "User.Read",
                    "ver" to "2.0"
                )
            )
            ok(token.serialize())
        },
        "/fake/custom" to {
            val req = it.asOAuth2HttpRequest()
            val token = mockOAuth2Server.anyToken(
                req.url.toIssuerUrl(),
                req.formParameters.map
            )
            ok(token.serialize())
        }
    )

    mockOAuth2Server.dispatcher = DelegatingDispatcher(
        MockOAuth2Dispatcher(mockOAuth2Server.config),
        pathDispatchers
    )

    mockOAuth2Server.start(InetSocketAddress(0).address, port = Config.port)
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            mockOAuth2Server.shutdown()
        }
    })
}

class DelegatingDispatcher(
    val defaultDispatcher: Dispatcher,
    val pathDispatchers: Map<String, PathDispather> = emptyMap()
) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        log.debug("path: ${request.toPath()}")
        return pathDispatchers[request.toPath()]
            ?.invoke(request)
            ?: defaultDispatcher.dispatch(request)
    }
}

internal fun ok(body: String?) =
    response(200, Headers.headersOf(), body)

internal fun response(status: Int, headers: Headers = Headers.headersOf(), body: String? = null): MockResponse =
    MockResponse()
        .setResponseCode(status)
        .setHeaders(headers)
        .apply {
            body?.let { setBody(it) }
        }

internal fun RecordedRequest.toPath(): String? =
    this.requestUrl?.pathSegments?.joinToString("/", "/")

internal fun RecordedRequest.param(name: String): String? =
    this.requestUrl?.queryParameter(name)

internal infix fun RecordedRequest.shouldHavePath(path: String): Boolean =
    this.requestUrl?.pathSegments == path.toPathSegments()

internal fun String.toPathSegments(): List<String> =
    this.removePrefix("/")
        .removeSuffix("/")
        .split("/")
        .toList()

internal fun String.fromEnv(): String? = System.getenv(this)
internal fun String.fromEnvOrFail(): String = fromEnv() ?: throw RuntimeException("could not find environment var $this")

object Config {
    val host: InetAddress = "OAUTH2_SERVER_HOST".fromEnv()?.let {
        InetAddress.getByName(it)
    } ?: InetSocketAddress(0).address

    val port: Int = "OAUTH2_SERVER_PORT".fromEnv()?.toInt() ?: 8080
}
