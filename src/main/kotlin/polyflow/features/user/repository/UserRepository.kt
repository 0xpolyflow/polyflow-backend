package polyflow.features.user.repository

import polyflow.features.user.model.params.CreateUserParams
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.id.UserId
import polyflow.util.UtcDateTime

interface UserRepository {
    fun store(params: CreateUserParams): User
    fun getById(id: UserId): User?
    fun getByStripeCustomerId(stripeCustomerId: String): User?
    fun getByEmail(email: String): User?
    fun setVerifiedAt(userId: UserId, verifiedAt: UtcDateTime)
    fun setPassword(userId: UserId, passwordHash: String)
    fun setStripeCustomerId(userId: UserId, stripeCustomerId: String)
    fun updateAccountLimits(userId: UserId, domainLimit: Int, seatLimit: Int)
    fun getUsedDomainsById(userId: UserId): Int
    fun getUsedSeatsById(userId: UserId): Int
}
