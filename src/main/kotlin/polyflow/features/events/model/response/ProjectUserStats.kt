package polyflow.features.events.model.response

data class ProjectUserStats(
    val totalUsers: Int,
    val usersWithWallet: Int,
    val usersWithConnectedWallet: Int,
    val usersWithExecutedTx: Int,
    val usersWithMultipleExecutedTx: Int
)
