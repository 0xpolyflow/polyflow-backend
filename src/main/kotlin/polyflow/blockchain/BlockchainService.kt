package polyflow.blockchain

import polyflow.util.AccountBalance
import polyflow.util.BlockName
import polyflow.util.BlockParameter
import polyflow.util.ContractAddress
import polyflow.util.UsdValue
import polyflow.util.WalletAddress

interface BlockchainService {
    fun fetchAccountBalance(
        chainSpec: ChainSpec,
        walletAddress: WalletAddress,
        blockParameter: BlockParameter = BlockName.LATEST
    ): AccountBalance

    fun fetchErc20OrErc721AccountBalances(
        chainSpec: ChainSpec,
        contractAddresses: List<ContractAddress>,
        walletAddress: WalletAddress,
        blockParameter: BlockParameter = BlockName.LATEST
    ): Map<ContractAddress, AccountBalance>

    fun fetchCurrentUsdPrice(
        chainSpec: ChainSpec,
        priceFeedContract: ContractAddress,
        blockParameter: BlockParameter = BlockName.LATEST
    ): UsdValue
}
