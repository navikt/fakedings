package fakedings

import java.net.InetAddress
import java.net.InetSocketAddress
import mu.KotlinLogging
import no.nav.security.mock.oauth2.MockOAuth2Dispatcher
import no.nav.security.mock.oauth2.MockOAuth2Server
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
          response(200, "alive")
        },
        "/login/client_assertion" to {
            val clientId: String = it.param("client_id") ?: "notfound"
            val jwt = clientAssertion(
                clientId,
                mockOAuth2Server.tokenEndpointUrl("tokenx").toString(),
                rsaKey
            )
            response(200, jwt.serialize())
        },
        "/login/idporten" to {
            val pid: String = it.param("pid") ?: "notfound"
            val acr: String = it.param("acr") ?: "Level4"
            val token = mockOAuth2Server.issueIdportenToken(pid, acr)
            response(200, token.serialize())
        },
        "/login/aad" to { req ->
            val preferredUsername: String = req.param("preferred_username") ?: "notfound"
            val name: String = req.param("name") ?: "notfound"
            val token = mockOAuth2Server.issueAADToken(preferredUsername, name)
            response(200, token.serialize())
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
        return pathDispatchers[request.toPath()]?.also {

        }
            ?.invoke(request)
            ?: defaultDispatcher.dispatch(request)
    }
}

internal fun response(status: Int, body: String? = null): MockResponse =
    MockResponse()
        .setResponseCode(status)
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
