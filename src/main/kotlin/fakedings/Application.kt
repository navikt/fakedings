package fakedings

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

    mockOAuth2Server.start(port = 1111)
}

class DelegatingDispatcher(
    val defaultDispatcher: Dispatcher,
    val pathDispatchers: Map<String, (RecordedRequest) -> MockResponse> = emptyMap()
) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        return pathDispatchers[request.toPath()]
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
    this.requestUrl?.pathSegments?.joinToString("/")

internal fun RecordedRequest.param(name: String): String? =
    this.requestUrl?.queryParameter(name)

internal infix fun RecordedRequest.shouldHavePath(path: String): Boolean =
    this.requestUrl?.pathSegments == path.toPathSegments()

internal fun String.toPathSegments(): List<String> =
    this.removePrefix("/")
        .removeSuffix("/")
        .split("/")
        .toList()
