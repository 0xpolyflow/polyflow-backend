package polyflow.features.events.model.response

data class WalletConnectionsAndTransactionsInfo(
    val name: String,
    val totalWalletConnections: Int,
    val uniqueWalletConnections: Int,
    val executedTransactions: Int
)
