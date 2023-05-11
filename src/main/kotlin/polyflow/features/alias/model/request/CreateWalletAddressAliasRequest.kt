package polyflow.features.alias.model.request

import polyflow.config.validation.MaxStringSize
import polyflow.config.validation.ValidEthAddress
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreateWalletAddressAliasRequest(
    @field:NotNull
    @field:ValidEthAddress
    val walletAddress: String,

    @field:NotNull
    @field:NotBlank
    @field:MaxStringSize
    val alias: String
)
