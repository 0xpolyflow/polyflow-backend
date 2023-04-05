package polyflow.config.authentication

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import polyflow.exception.JwtTokenException
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Date
import java.util.UUID
import kotlin.time.Duration

object JwtTokenUtils {

    private const val ID_KEY = "id"
    private const val EMAIL_KEY = "email"
    private const val JWT_SUBJECT = "Polyflow"

    fun decodeToken(token: String, publicKey: RSAPublicKey): JwtAuthToken {
        try {
            val parser = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
            val claims = parser.parseClaimsJws(token)
            val expiration = claims.body.expiration

            if (Date().after(expiration)) {
                throw JwtTokenException("JWT token expired")
            }

            val userId = claims.getClaim(ID_KEY) { UserId(UUID.fromString(it)) }
            val email = claims.getClaim(EMAIL_KEY) { it }

            return JwtAuthToken(
                token = token,
                id = userId,
                email = email,
                validUntil = UtcDateTime.ofInstant(expiration.toInstant())
            )
        } catch (_: JwtException) {
            throw JwtTokenException("Could not validate JWT token")
        }
    }

    fun encodeToken(id: UserId, email: String, privateKey: RSAPrivateKey, tokenValidity: Duration): JwtAuthToken {
        val issuedAt = Date()
        val validUntil = Date.from(issuedAt.toInstant().plusMillis(tokenValidity.inWholeMilliseconds))
        val token = Jwts.builder()
            .setSubject(JWT_SUBJECT)
            .claim(ID_KEY, id.value.toString())
            .claim(EMAIL_KEY, email)
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .setIssuedAt(issuedAt)
            .setExpiration(validUntil)
            .compact()

        return JwtAuthToken(
            token = token,
            id = id,
            email = email,
            validUntil = UtcDateTime.ofInstant(validUntil.toInstant())
        )
    }

    private fun <T> Jws<Claims>.getClaim(key: String, transform: (String) -> T): T =
        (body[key] as? String)?.let(transform) ?: throw JwtTokenException("Invalid JWT token format")
}
