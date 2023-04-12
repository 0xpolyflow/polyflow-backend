package polyflow.features.user.repository

import mu.KLogging
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.util.postgres.PostgresDSL
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import polyflow.exception.UserAlreadyExistsException
import polyflow.features.user.model.params.CreateUserParams
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.id.UserId
import polyflow.generated.jooq.tables.ProjectTable
import polyflow.generated.jooq.tables.UserProjectAccessTable
import polyflow.generated.jooq.tables.UserTable
import polyflow.generated.jooq.tables.records.UserRecord
import polyflow.util.UtcDateTime

@Repository
class JooqUserRepository(private val dslContext: DSLContext) : UserRepository { // TODO test

    companion object : KLogging()

    override fun store(params: CreateUserParams): User {
        logger.info { "Store user: $params" }
        val record = UserRecord(
            id = params.id,
            email = params.email,
            passwordHash = params.passwordHash,
            accountType = params.accountType,
            registeredAt = params.registeredAt,
            verifiedAt = null,
            stripeCustomerId = null,
            totalDomainLimit = 0,
            totalSeatLimit = 0
        )

        try {
            dslContext.executeInsert(record)
        } catch (_: DuplicateKeyException) {
            throw UserAlreadyExistsException()
        }

        return record.toModel()
    }

    override fun getById(id: UserId): User? {
        logger.debug { "Get user by id: $id" }
        return dslContext.selectFrom(UserTable)
            .where(UserTable.ID.eq(id))
            .fetchOne { it.toModel() }
    }

    override fun getByStripeCustomerId(stripeCustomerId: String): User? {
        logger.debug { "Get user by stripeCustomerId: $stripeCustomerId" }
        return dslContext.selectFrom(UserTable)
            .where(UserTable.STRIPE_CUSTOMER_ID.eq(stripeCustomerId))
            .fetchOne { it.toModel() }
    }

    override fun getByEmail(email: String): User? {
        logger.debug { "Get user by email: $email" }
        return dslContext.selectFrom(UserTable)
            .where(UserTable.EMAIL.eq(email))
            .fetchOne { it.toModel() }
    }

    override fun setVerifiedAt(userId: UserId, verifiedAt: UtcDateTime) {
        logger.info { "Set user verifiedAt: $verifiedAt, userId: $userId" }
        dslContext.update(UserTable)
            .set(UserTable.VERIFIED_AT, verifiedAt)
            .where(UserTable.ID.eq(userId))
            .execute()
    }

    override fun setPassword(userId: UserId, passwordHash: String) {
        logger.info { "Set new password for userId: $userId" }
        dslContext.update(UserTable)
            .set(UserTable.PASSWORD_HASH, passwordHash)
            .where(UserTable.ID.eq(userId))
            .execute()
    }

    override fun setStripeCustomerId(userId: UserId, stripeCustomerId: String) {
        logger.info { "Set stripe customer for userId: $userId, stripeCustomerId: $stripeCustomerId" }
        dslContext.update(UserTable)
            .set(UserTable.STRIPE_CUSTOMER_ID, stripeCustomerId)
            .where(
                DSL.and(
                    UserTable.ID.eq(userId),
                    UserTable.STRIPE_CUSTOMER_ID.isNull
                )
            )
            .execute()
    }

    override fun updateAccountLimits(userId: UserId, domainLimit: Int, seatLimit: Int) {
        logger.info { "Update account limits for userId: $userId, domainLimit: $domainLimit, seatLimit: $seatLimit" }
        dslContext.update(UserTable)
            .set(UserTable.TOTAL_DOMAIN_LIMIT, domainLimit)
            .set(UserTable.TOTAL_SEAT_LIMIT, seatLimit)
            .where(UserTable.ID.eq(userId))
            .execute()
    }

    override fun getUsedDomainsById(userId: UserId): Int {
        logger.debug { "Get number of used domains by userId: $userId" }

        val sum = DSL.sum(PostgresDSL.arrayLength(ProjectTable.WHITELISTED_DOMAINS)).`as`("sum")

        return dslContext.select(sum)
            .from(ProjectTable)
            .where(ProjectTable.OWNER_ID.eq(userId))
            .fetchOne(sum)
            ?.intValueExact() ?: 0
    }

    override fun getUsedSeatsById(userId: UserId): Int {
        logger.debug { "Get number of used seats by userId: $userId" }

        return dslContext.selectCount()
            .from(
                UserProjectAccessTable.join(ProjectTable).on(UserProjectAccessTable.PROJECT_ID.eq(ProjectTable.ID))
            )
            .where(
                DSL.and(
                    ProjectTable.OWNER_ID.eq(userId),
                    UserProjectAccessTable.USER_ID.ne(userId)
                )
            )
            .fetchOne(DSL.count()) ?: 0
    }

    private fun UserRecord.toModel(): User =
        User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            accountType = accountType,
            registeredAt = registeredAt,
            verifiedAt = verifiedAt,
            stripeCustomerId = stripeCustomerId,
            totalDomainLimit = totalDomainLimit,
            totalSeatLimit = totalSeatLimit
        )
}
