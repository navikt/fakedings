package fakedings

import com.nimbusds.jwt.JWTParser
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest {
    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    private var serverBaseUrl: String = ""

    @BeforeAll
    fun setUp() {
        // Set config values for testing
        System.setProperty("OAUTH2_SERVER_HOST", "localhost")
        System.setProperty("OAUTH2_SERVER_PORT", "0") // Will pick an available port

        // Initialize the application for testing
        main()

        // Get the actual port that was assigned
        serverBaseUrl = "http://localhost:${Config.port}"
    }

    @AfterAll
    fun tearDown() {
        // Clean up will be handled by the shutdown hook in main()
    }

    @Test
    fun `test health check endpoint`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/internal/isalive")
                .get()
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)
            assertEquals(expected = "alive", actual = response.body?.string())
        }
    }

    @Test
    fun `test client assertion endpoint default values`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/client_assertion")
                .get()
                .build()

        client.newCall(request).execute().use { response: Response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("notfound"), actual = claims.audience)
            assertEquals(expected = "notfound", actual = claims.getStringClaim("sub"))
            assertEquals(expected = "notfound", actual = claims.getStringClaim("iss"))
        }
    }

    @Test
    fun `test idporten token endpoint default values`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/idporten")
                .get()
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("notfound"), actual = claims.audience)
            assertContentEquals(expected = listOf("BankID"), actual = claims.getStringListClaim("amr"))
            assertEquals(expected = "notfound", actual = claims.getStringClaim("pid"))
            assertEquals(expected = "idporten-loa-high", actual = claims.getStringClaim("acr"))
            assertEquals(expected = "notfound", actual = claims.getStringClaim("client_id"))
            assertEquals(expected = "nb", actual = claims.getStringClaim("locale"))
            // UUID and time-based values are random, so we don't assert them
            assertTrue(actual = claims.claims.containsKey("sub"))
            assertTrue(actual = claims.claims.containsKey("at_hash"))
            assertTrue(actual = claims.claims.containsKey("sid"))
            assertTrue(actual = claims.claims.containsKey("auth_time"))
        }
    }

    @Test
    fun `test aad token endpoint default values`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/aad")
                .get()
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("receiver-client-id"), actual = claims.audience)
            assertEquals(expected = "notfound", actual = claims.getStringClaim("preferred_username"))
            assertEquals(expected = "notfound", actual = claims.getStringClaim("name"))
            assertEquals(expected = "consumer-cilent-id", actual = claims.getStringClaim("azp"))
            assertEquals(expected = "1", actual = claims.getStringClaim("azpacr"))
            assertEquals(expected = "User.Read", actual = claims.getStringClaim("scp"))
            assertEquals(expected = "2.0", actual = claims.getStringClaim("ver"))
            // UUID-based values are random, so we just check they exist
            assertTrue(actual = claims.claims.containsKey("sub"))
            assertTrue(actual = claims.claims.containsKey("aio"))
            assertTrue(actual = claims.claims.containsKey("oid"))
            // Optional fields should be absent
            assertFalse(actual = claims.claims.containsKey("NAVident"))
            assertFalse(actual = claims.claims.containsKey("groups"))
        }
    }

    @Test
    fun `test tokenx endpoint default values`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/tokenx")
                .get()
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("notfound"), actual = claims.audience)
            assertContentEquals(expected = listOf("BankID"), actual = claims.getStringListClaim("amr"))
            assertEquals(expected = "notfound", actual = claims.getStringClaim("pid"))
            assertEquals(expected = "notfound", actual = claims.getStringClaim("client_id"))
            assertEquals(expected = "Level4", actual = claims.getStringClaim("acr"))
            assertEquals(expected = "nb", actual = claims.getStringClaim("locale"))
            assertEquals(expected = "Bearer", actual = claims.getStringClaim("token_type"))
            assertEquals(expected = "openid", actual = claims.getStringClaim("scope"))
            assertEquals(expected = "889640782", actual = claims.getStringClaim("client_orgno"))
            assertTrue(actual = claims.getStringClaim("idp").startsWith("http://localhost"))
            // UUID-based values are random, so we just check they exist
            assertTrue(actual = claims.claims.containsKey("sub"))
            assertTrue(actual = claims.claims.containsKey("jti"))
        }
    }

    @Test
    fun `test custom token endpoint with empty form`() {
        val formBody =
            FormBody
                .Builder()
                .build()

        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/custom")
                .post(formBody)
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            // Custom token has several default JWT claims
            assertTrue(actual = claims.claims.containsKey("iss"))
            assertTrue(actual = claims.claims.containsKey("iat"))
            assertTrue(actual = claims.claims.containsKey("exp"))
            assertTrue(actual = claims.claims.containsKey("nbf"))
            assertTrue(actual = claims.claims.containsKey("jti"))
        }
    }

    @Test
    fun `test invalid path returns proper error response`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/non-existent-path")
                .get()
                .build()

        client.newCall(request).execute().use { response ->
            assertTrue(actual = response.code >= 400)
        }
    }

    @Test
    fun `test client assertion endpoint with query parameter overrides`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/client_assertion?client_id=custom-client-id&aud=custom-audience")
                .get()
                .build()

        client.newCall(request).execute().use { response: Response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("custom-audience"), actual = claims.audience)
            assertEquals(expected = "custom-client-id", actual = claims.getStringClaim("sub"))
            assertEquals(expected = "custom-client-id", actual = claims.getStringClaim("iss"))
        }
    }

    @Test
    fun `test idporten token endpoint with query parameter overrides`() {
        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/idporten?pid=12345678901&client_id=custom-client-id&acr=idporten-loa-substantial&locale=en&amr=Commfides")
                .get()
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("Commfides"), actual = claims.getStringListClaim("amr"))
            assertEquals(expected = "12345678901", actual = claims.getStringClaim("pid"))
            assertEquals(expected = "idporten-loa-substantial", actual = claims.getStringClaim("acr"))
            assertEquals(expected = "custom-client-id", actual = claims.getStringClaim("client_id"))
            assertEquals(expected = "en", actual = claims.getStringClaim("locale"))
        }
    }

    @Test
    fun `test aad token endpoint with query parameter overrides`() {
        val request =
            Request
                .Builder()
                .url(
                    "$serverBaseUrl/fake/aad?preferred_username=custom-user@example.com&name=Custom+User&NAVident=X123456&groups=group1,group2,group3&aud=custom-audience&azp=custom-azp",
                ).get()
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("custom-audience"), actual = claims.audience)
            assertContentEquals(expected = listOf("group1", "group2", "group3"), actual = claims.getStringListClaim("groups"))
            assertEquals(expected = "custom-user@example.com", actual = claims.getStringClaim("preferred_username"))
            assertEquals(expected = "Custom User", actual = claims.getStringClaim("name"))
            assertEquals(expected = "X123456", actual = claims.getStringClaim("NAVident"))
            assertEquals(expected = "custom-azp", actual = claims.getStringClaim("azp"))
        }
    }

    @Test
    fun `test tokenx endpoint with query parameter overrides`() {
        val request =
            Request
                .Builder()
                .url(
                    "$serverBaseUrl/fake/tokenx?client_id=custom-client-id&pid=12345678901&aud=custom-audience&acr=Level3&locale=en&amr=BankIDMobil&idp=https://custom-idp.example.com",
                ).get()
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("custom-audience"), actual = claims.audience)
            assertContentEquals(expected = listOf("BankIDMobil"), actual = claims.getStringListClaim("amr"))
            assertEquals(expected = "12345678901", actual = claims.getStringClaim("pid"))
            assertEquals(expected = "custom-client-id", actual = claims.getStringClaim("client_id"))
            assertEquals(expected = "Level3", actual = claims.getStringClaim("acr"))
            assertEquals(expected = "en", actual = claims.getStringClaim("locale"))
            assertEquals(expected = "https://custom-idp.example.com", actual = claims.getStringClaim("idp"))
        }
    }

    @Test
    fun `test custom token endpoint with form parameter overrides`() {
        val formBody =
            FormBody
                .Builder()
                .add("sub", "custom-subject")
                .add("aud", "custom-audience")
                .add("name", "Custom User")
                .add("email", "custom@example.com")
                .add("groups", "group1,group2")
                .add("roles", "admin,user,editor")
                .add("custom_claim", "custom value")
                .add("number_claim", "42")
                .build()

        val request =
            Request
                .Builder()
                .url("$serverBaseUrl/fake/custom")
                .post(formBody)
                .build()

        client.newCall(request).execute().use { response ->
            assertEquals(expected = 200, actual = response.code)

            val jwt = response.body?.string() ?: ""
            val parsedJwt = JWTParser.parse(jwt)
            val claims = parsedJwt.jwtClaimsSet

            assertContentEquals(expected = listOf("custom-audience"), actual = claims.audience)
            assertEquals(expected = "custom-subject", actual = claims.getStringClaim("sub"))
            assertEquals(expected = "Custom User", actual = claims.getStringClaim("name"))
            assertEquals(expected = "custom@example.com", actual = claims.getStringClaim("email"))
            assertEquals(expected = "group1,group2", actual = claims.getStringClaim("groups"))
            assertEquals(expected = "admin,user,editor", actual = claims.getStringClaim("roles"))
            assertEquals(expected = "custom value", actual = claims.getStringClaim("custom_claim"))

            // Default JWT claims should still be present
            assertTrue(actual = claims.claims.containsKey("iss"))
            assertTrue(actual = claims.claims.containsKey("iat"))
            assertTrue(actual = claims.claims.containsKey("exp"))
            assertTrue(actual = claims.claims.containsKey("nbf"))
            assertTrue(actual = claims.claims.containsKey("jti"))
        }
    }
}
