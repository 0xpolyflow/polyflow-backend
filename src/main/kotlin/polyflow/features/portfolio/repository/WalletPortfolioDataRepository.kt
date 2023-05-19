package polyflow.features.portfolio.repository

import polyflow.features.portfolio.model.result.WalletPortfolioData
import polyflow.util.WalletAddress

interface WalletPortfolioDataRepository {
    fun upsertWalletPortfolioData(data: WalletPortfolioData)
    fun getWalletPortfolioData(walletAddress: WalletAddress): WalletPortfolioData?
}
