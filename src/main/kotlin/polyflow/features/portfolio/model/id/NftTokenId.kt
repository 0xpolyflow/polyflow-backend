package polyflow.features.portfolio.model.id

import polyflow.util.ChainId
import polyflow.util.ContractAddress
import polyflow.util.NftId

data class NftTokenId(val tokenAddress: ContractAddress, val tokenId: NftId, val chainId: ChainId)
