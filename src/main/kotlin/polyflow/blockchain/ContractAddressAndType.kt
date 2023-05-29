package polyflow.blockchain

import polyflow.util.ContractAddress

data class ContractAddressAndType(
    val contractAddress: ContractAddress,
    val isNft: Boolean
)
