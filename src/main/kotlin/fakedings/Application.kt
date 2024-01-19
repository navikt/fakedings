package fakedings

import com.nimbusds.oauth2.sdk.OAuth2Error
import mu.KotlinLogging
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.OAuth2Config
import no.nav.security.mock.oauth2.OAuth2Exception
import no.nav.security.mock.oauth2.extensions.asOAuth2HttpRequest
import no.nav.security.mock.oauth2.extensions.toIssuerUrl
import no.nav.security.mock.oauth2.http.OAuth2HttpResponse
import no.nav.security.mock.oauth2.http.OAuth2HttpServer
import no.nav.security.mock.oauth2.http.RequestHandler
import no.nav.security.mock.oauth2.http.Ssl
import no.nav.security.mock.oauth2.token.OAuth2TokenProvider
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Instant
import java.util.Date
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

private val log = KotlinLogging.logger { }

typealias PathDispather = (RecordedRequest) -> MockResponse

val rsaKey = createRSAKey("clientAssertionKey")

fun main() {
    val tokenProvider = OAuth2TokenProvider()
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
            val acr: String = it.param("acr") ?: "idporten-loa-high"
            val locale: String = it.param("locale") ?: "nb"
            val amr: String = it.param("amr") ?: "BankID"
            val clientId = it.param("client_id") ?: "notfound"

            val token = tokenProvider.fakeToken(
                req.url.fakeIssuerUrl(),
                mapOf(
                    "sub" to UUID.randomUUID().toString(),
                    "aud" to "notfound",
                    "at_hash" to UUID.randomUUID().toString(),
                    "amr" to listOf(amr),
                    "pid" to pid,
                    "locale" to locale,
                    "acr" to acr,
                    "sid" to UUID.randomUUID().toString(),
                    "auth_time" to Date.from(Instant.now()),
                    "client_id" to clientId,
                ),
            )
            ok(token.serialize())
        },
        "/fake/aad" to {
            val req = it.asOAuth2HttpRequest()
            val preferredUsername: String = it.param("preferred_username") ?: "notfound"
            val name: String = it.param("name") ?: "notfound"
            val aud = it.param("aud") ?: "receiver-client-id"
            val azp = it.param("azp") ?: "consumer-cilent-id"
            val navIdent: String? = it.param("NAVident")
            val groups: List<String>? = it.param("groups")?.split(",")

            val token = tokenProvider.fakeToken(
                req.url.fakeIssuerUrl(),
                mapOf(
                    "sub" to UUID.randomUUID().toString(),
                    "aud" to aud,
                    "aio" to UUID.randomUUID().toString(),
                    "azpacr" to "1",
                    "azp" to azp,
                    "name" to name,
                    "oid" to UUID.randomUUID().toString(),
                    "preferred_username" to preferredUsername,
                    "scp" to "User.Read",
                    "ver" to "2.0",
                ) + mapOfNotNullValues(
                    "NAVident" to navIdent,
                    "groups" to groups
                )
            )
            ok(token.serialize())
        },
        "/fake/tokenx" to {
            val req = it.asOAuth2HttpRequest()
            val clientId = it.param("client_id") ?: "notfound"
            val pid = it.param("pid") ?: "notfound"
            val aud = it.param("aud") ?: "notfound"
            val acr = it.param("acr") ?: "Level4"
            val locale: String = it.param("locale") ?: "nb"
            val amr: String = it.param("amr") ?: "BankID"
            val idp: String = it.param("idp") ?: req.url.fakeIssuerUrl().toString()

            val token = tokenProvider.fakeToken(
                req.url.fakeIssuerUrl(),
                mapOf(
                    "sub" to UUID.randomUUID().toString(),
                    "amr" to listOf(amr),
                    "locale" to locale,
                    "pid" to pid,
                    "token_type" to "Bearer",
                    "client_id" to clientId,
                    "aud" to aud,
                    "acr" to acr,
                    "idp" to idp,
                    "scope" to "openid",
                    "client_orgno" to "889640782",
                    "jti" to UUID.randomUUID().toString(),
                ),
            )
            ok(token.serialize())
        },

        "/fake/custom" to {
            val req = it.asOAuth2HttpRequest()
            val token = tokenProvider.fakeToken(
                req.url.fakeIssuerUrl(),
                req.formParameters.map,
            )
            ok(token.serialize())
        },
    )

    val mockOAuth2Server = MockOAuth2Server(OAuth2Config(tokenProvider = tokenProvider, httpServer = MockWebServerWrapper(pathDispatchers = pathDispatchers)))
    mockOAuth2Server.start(InetSocketAddress(0).address, port = Config.port)
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            mockOAuth2Server.shutdown()
        }
    })
}

internal fun <Key, Value>mapOfNotNullValues(vararg entries: Pair<Key, Value?>): Map<Key, Value> =
    entries.mapNotNull { (key, value) -> if (value == null) null else key to value }.toMap()

class MockWebServerWrapper @JvmOverloads constructor(
    val ssl: Ssl? = null,
    val pathDispatchers: Map<String, PathDispather>,
) : OAuth2HttpServer {
    private val mockWebServer: MockWebServer = MockWebServer()

    override fun start(inetAddress: InetAddress, port: Int, requestHandler: RequestHandler): OAuth2HttpServer = apply {
        mockWebServer.start(inetAddress, port)
        mockWebServer.dispatcher = DelegatingDispatcher(
            MockWebServerDispatcher(requestHandler),
            pathDispatchers,
        )
        if (ssl != null) {
            mockWebServer.useHttps(ssl.sslContext().socketFactory, false)
        }
        log.debug { "started server on address=$inetAddress and port=${mockWebServer.port}, httpsEnabled=${ssl != null}" }
    }

    override fun stop(): OAuth2HttpServer = apply {
        mockWebServer.shutdown()
    }

    override fun port(): Int = mockWebServer.port

    override fun url(path: String): HttpUrl = mockWebServer.url(path)
    override fun sslConfig(): Ssl? = ssl

    internal class MockWebServerDispatcher(
        private val requestHandler: RequestHandler,
        private val responseQueue: BlockingQueue<MockResponse> = LinkedBlockingQueue(),
    ) : Dispatcher() {

        override fun dispatch(request: RecordedRequest): MockResponse =
            responseQueue.peek()?.let {
                responseQueue.take()
            } ?: requestHandler.invoke(request.asOAuth2HttpRequest()).toMockResponse()

        private fun OAuth2HttpResponse.toMockResponse(): MockResponse =
            MockResponse()
                .setHeaders(this.headers)
                .setResponseCode(this.status)
                .let {
                    if (this.body != null) it.setBody(this.body!!) else it.setBody("")
                }
    }
}

class DelegatingDispatcher(
    val defaultDispatcher: Dispatcher,
    val pathDispatchers: Map<String, PathDispather> = emptyMap(),
) : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        log.debug { "path: ${request.toPath()}" }
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

internal fun HttpUrl.fakeIssuerUrl() = this.toIssuerUrl().resolve("/fake") ?: throw OAuth2Exception(OAuth2Error.INVALID_REQUEST, "cannot resolve path '/fake'")

internal fun String.fromEnv(): String? = System.getenv(this)
internal fun String.fromEnvOrFail(): String = fromEnv() ?: throw RuntimeException("could not find environment var $this")

object Config {
    val host: InetAddress = "OAUTH2_SERVER_HOST".fromEnv()?.let {
        InetAddress.getByName(it)
    } ?: InetSocketAddress(0).address

    val port: Int = "OAUTH2_SERVER_PORT".fromEnv()?.toInt() ?: 8080
}
