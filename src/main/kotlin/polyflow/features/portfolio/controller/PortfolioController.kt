package polyflow.features.portfolio.controller

import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import polyflow.config.validation.ValidEthAddress
import polyflow.features.events.controller.Util
import polyflow.features.portfolio.model.response.WalletPortfolioResponse
import polyflow.features.portfolio.service.PortfolioService
import polyflow.features.user.repository.UserRepository
import polyflow.util.WalletAddress

@Validated
@Controller
class PortfolioController(
    private val portfolioService: PortfolioService,
    private val userRepository: UserRepository
) { // TODO test

    @MutationMapping
    fun refreshPortfolio(
        @ValidEthAddress @Argument walletAddress: String
    ): String {
        Util.resolveUser(userRepository)
        portfolioService.fetchAndStorePortfolio(WalletAddress(walletAddress), force = true)
        return "ACCEPTED"
    }

    @QueryMapping
    fun getPortfolio(
        @ValidEthAddress @Argument walletAddress: String
    ): WalletPortfolioResponse? {
        Util.resolveUser(userRepository)
        return portfolioService.fetchPortfolio(WalletAddress(walletAddress))
            ?.let { WalletPortfolioResponse(it) }
            ?: run {
                portfolioService.fetchAndStorePortfolio(WalletAddress(walletAddress), force = true)
                null
            }
    }
}
