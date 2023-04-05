package polyflow.config.authentication

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

class JwtAuthToken(
    val token: String,
    val id: UserId?,
    val email: String?,
    val validUntil: UtcDateTime
) : Authentication {

    companion object {
        private const val serialVersionUID: Long = 4492623951055121852L
    }

    override fun getName(): String = id.toString()

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf()

    override fun getCredentials(): String = token

    override fun getDetails(): Any? = null

    override fun getPrincipal(): UserId? = id

    override fun isAuthenticated(): Boolean = id != null

    override fun setAuthenticated(isAuthenticated: Boolean) {
        // not needed
    }
}
