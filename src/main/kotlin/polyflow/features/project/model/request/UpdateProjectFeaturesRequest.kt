package polyflow.features.project.model.request

data class UpdateProjectFeaturesRequest(
    val gasStation: Boolean?,
    val networkSwitcher: Boolean?,
    val connectWallet: Boolean?,
    val compliance: Boolean?,
    val errorMessages: Boolean?
)
