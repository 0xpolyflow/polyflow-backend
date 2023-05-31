package polyflow.features.events.model.response

data class WalletConnectionsAndTransactionsInfoForNetwork(
    val chainId: Long,
    val totalWalletConnections: Int,
    val uniqueWalletConnections: Int,
    val executedTransactions: Int
)
