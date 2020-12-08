package fakedings

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.AuthorizationGrant
import com.nimbusds.oauth2.sdk.GrantType
import com.nimbusds.oauth2.sdk.TokenRequest
import java.net.URI
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.time.Instant
import java.util.Date
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import okhttp3.HttpUrl

class MockGrant : AuthorizationGrant(GrantType("MockGrant")) {
    override fun toParameters(): MutableMap<String, MutableList<String>> = mutableMapOf()
}

internal fun MockOAuth2Server.anyToken(issuerUrl: HttpUrl, claims: Map<String, Any>, expiry: Duration = Duration.ofHours(1)): SignedJWT {
    val jwtClaimsSet = claims.toJwtClaimsSet()
    return this.config.tokenProvider.exchangeAccessToken(
        TokenRequest(URI.create("http://mockgrant"), MockGrant()),
        issuerUrl,
        jwtClaimsSet,
        DefaultOAuth2TokenCallback(
            audience = jwtClaimsSet.audience,
            expiry = expiry.toMillis()
        )
    )
}

internal fun clientAssertion(clientId: String, audience: String, rsaKey: RSAKey) =
    JWTClaimsSet.Builder()
        .subject(clientId)
        .issuer(clientId)
        .audience(audience)
        .notBeforeTime(Date.from(Instant.now()))
        .issueTime(Date.from(Instant.now()))
        .expirationTime(Date.from(Instant.now().plusSeconds(100)))
        .build().toSignedJWT(rsaKey)

internal fun JWTClaimsSet.toSignedJWT(key: RSAKey): SignedJWT =
    SignedJWT(
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(key.keyID)
            .type(JOSEObjectType.JWT).build(),
        this
    ).apply {
        this.sign(RSASSASigner(key.toPrivateKey()))
    }

internal fun createRSAKey(keyID: String) =
    KeyPairGenerator.getInstance("RSA").let {
        it.initialize(2048)
        it.generateKeyPair()
    }.let {
        RSAKey.Builder(it.public as RSAPublicKey)
            .privateKey(it.private as RSAPrivateKey)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(keyID)
            .build()
    }

internal fun Map<String, Any>.toJwtClaimsSet(): JWTClaimsSet =
    JWTClaimsSet.Builder()
        .apply {
            this@toJwtClaimsSet.forEach {
                this.claim(it.key, it.value)
            }
        }.build()
