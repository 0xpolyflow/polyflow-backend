import org.gradle.configurationcache.extensions.capitalized

object ForcedJooqTypes {

    data class JooqType(
        val userType: String,
        val includeExpression: String,
        val includeTypes: String,
        val converter: String
    ) {
        constructor(userType: String, includeExpression: String, includeTypes: String) : this(
            userType, includeExpression, includeTypes, userType + "Converter"
        )
    }

    private val domainIdTypes = listOf(
        "PROJECT_FEATURES_ID",
        "PROJECT_ID",
        "USER_ID"
    )

    val types = listOf(
        JooqType(
            userType = "polyflow.util.ChainId",
            includeExpression = "chain_id",
            includeTypes = "BIGINT"
        ),
        JooqType(
            userType = "polyflow.util.ContractAddress",
            includeExpression = "token_address|.*_contract_address|contract_address",
            includeTypes = "VARCHAR"
        ),
        JooqType(
            userType = "polyflow.util.WalletAddress",
            includeExpression = ".*_address",
            includeTypes = "VARCHAR"
        ),
        JooqType(
            userType = "polyflow.util.Balance",
            includeExpression = ".*_amount|amount",
            includeTypes = "NUMERIC"
        ),
        JooqType(
            userType = "polyflow.util.BlockNumber",
            includeExpression = "block_number",
            includeTypes = "NUMERIC"
        ),
        JooqType(
            userType = "polyflow.util.TransactionHash",
            includeExpression = ".*tx_hash",
            includeTypes = "VARCHAR"
        ),
        JooqType(
            userType = "polyflow.util.UtcDateTime",
            includeExpression = ".*",
            includeTypes = "TIMESTAMPTZ"
        )
    ) + domainIdTypes.map {
        val typeName = it.split("_").joinToString("") { it.toLowerCase().capitalized() }

        JooqType(
            userType = "${Configurations.Jooq.packageName}.id.$typeName",
            converter = "${Configurations.Jooq.packageName}.converters.${typeName}Converter",
            includeExpression = ".*",
            includeTypes = it
        )
    }
}
