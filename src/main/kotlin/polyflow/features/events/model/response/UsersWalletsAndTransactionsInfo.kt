package polyflow.features.events.model.response

data class UsersWalletsAndTransactionsInfo(
    val name: String,
    val totalUsers: Int,
    val usersWithWallet: Int,
    val usersWithConnectedWallet: Int,
    val totalWalletConnections: Int,
    val uniqueWalletConnections: Int,
    val executedTransactions: Int,
    val usersWithExecutedTx: Int
)
