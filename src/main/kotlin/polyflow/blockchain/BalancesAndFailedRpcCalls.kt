package polyflow.blockchain

import polyflow.features.portfolio.model.result.AssetRpcCall
import polyflow.util.AccountBalance
import polyflow.util.ContractAddress

data class BalancesAndFailedRpcCalls(
    val balances: Map<ContractAddress, AccountBalance>,
    val failedRpcCalls: List<AssetRpcCall>
)
