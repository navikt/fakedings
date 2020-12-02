package fakedings

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Date
import java.util.UUID
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.mock.oauth2.token.OAuth2TokenCallback

internal fun MockOAuth2Server.issueIdportenToken(pid: String, acr: String) =
    issueToken(
        issuerId = "idporten",
        subject = UUID.randomUUID().toString(),
        claims = mapOf(
            "at_hash" to UUID.randomUUID().toString(),
            "amr" to listOf("BankId"),
            "pid" to pid,
            "locale" to "nb",
            "acr" to acr,
            "sid" to UUID.randomUUID().toString(),
            "auth_time" to Date.from(Instant.now())
        )
    )

internal fun MockOAuth2Server.issueAADToken(preferredUsername: String, name: String) =
    issueToken(
        issuerId = "aad",
        subject = UUID.randomUUID().toString(),
        claims = mapOf(
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

val idportenTokenCallback: OAuth2TokenCallback =
    DefaultOAuth2TokenCallback(
        issuerId = "idporten",
        claims = mapOf(
            "at_hash" to UUID.randomUUID().toString(),
            "amr" to listOf("BankId"),

        )
    )
