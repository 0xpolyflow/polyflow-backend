package polyflow.features.user.repository

import mu.KLogging
import org.jooq.DSLContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import polyflow.exception.UserAlreadyExistsException
import polyflow.features.user.model.params.CreateUserParams
import polyflow.features.user.model.result.User
import polyflow.generated.jooq.id.UserId
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
            verifiedAt = null
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

    private fun UserRecord.toModel(): User =
        User(
            id = id,
            email = email,
            passwordHash = passwordHash,
            accountType = accountType,
            registeredAt = registeredAt,
            verifiedAt = verifiedAt
        )
}
