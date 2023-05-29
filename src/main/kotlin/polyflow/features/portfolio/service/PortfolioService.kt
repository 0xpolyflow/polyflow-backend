package polyflow.features.portfolio.service

import polyflow.features.portfolio.model.result.WalletPortfolioDataAndValue
import polyflow.util.WalletAddress

interface PortfolioService {
    fun fetchAndStorePortfolio(walletAddress: WalletAddress, force: Boolean)
    fun fetchPortfolio(walletAddress: WalletAddress): WalletPortfolioDataAndValue?
}
