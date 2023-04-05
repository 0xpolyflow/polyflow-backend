package polyflow.features.user.repository

import mu.KLogging
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import polyflow.config.UserAccountProperties
import polyflow.features.user.model.SecureUserToken
import polyflow.generated.jooq.id.UserId
import polyflow.generated.jooq.tables.UserPasswordResetTokenTable
import polyflow.generated.jooq.tables.UserVerificationTokenTable
import polyflow.generated.jooq.tables.records.UserPasswordResetTokenRecord
import polyflow.generated.jooq.tables.records.UserVerificationTokenRecord
import polyflow.util.UtcDateTime

@Repository
class JooqSecureUserTokenRepository(
    private val dslContext: DSLContext,
    private val userAccountProperties: UserAccountProperties
) : SecureUserTokenRepository { // TODO test

    companion object : KLogging()

    override fun storeVerificationToken(token: SecureUserToken): SecureUserToken {
        logger.info { "Store user verification token: $token" }

        val table = UserVerificationTokenTable

        dslContext.deleteFrom(table)
            .where(table.USER_ID.eq(token.userId))
            .execute()

        dslContext.executeInsert(
            UserVerificationTokenRecord(
                token = token.token,
                userId = token.userId,
                createdAt = token.createdAt
            )
        )

        return token
    }

    override fun storePasswordResetToken(token: SecureUserToken): SecureUserToken {
        logger.info { "Store user password reset token: $token" }

        val table = UserPasswordResetTokenTable

        dslContext.deleteFrom(table)
            .where(table.USER_ID.eq(token.userId))
            .execute()

        dslContext.executeInsert(
            UserPasswordResetTokenRecord(
                token = token.token,
                userId = token.userId,
                createdAt = token.createdAt
            )
        )

        return token
    }

    override fun useVerificationToken(token: String, now: UtcDateTime): UserId? {
        logger.info { "Use user verification token: $token, now: $now" }

        val table = UserVerificationTokenTable

        return dslContext.deleteFrom(table)
            .where(
                DSL.and(
                    table.TOKEN.eq(token),
                    table.CREATED_AT.gt(now - userAccountProperties.verificationTokenDuration)
                )
            )
            .returning(table.USER_ID)
            .fetchOne(table.USER_ID)
    }

    override fun usePasswordResetToken(token: String, now: UtcDateTime): UserId? {
        logger.info { "Use user password reset token: $token, now: $now" }

        val table = UserPasswordResetTokenTable

        return dslContext.deleteFrom(table)
            .where(
                DSL.and(
                    table.TOKEN.eq(token),
                    table.CREATED_AT.gt(now - userAccountProperties.verificationTokenDuration)
                )
            )
            .returning(table.USER_ID)
            .fetchOne(table.USER_ID)
    }
}
