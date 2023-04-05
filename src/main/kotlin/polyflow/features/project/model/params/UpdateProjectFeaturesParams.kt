package polyflow.features.project.model.params

data class UpdateProjectFeaturesParams(
    val gasStation: Boolean?,
    val networkSwitcher: Boolean?,
    val connectWallet: Boolean?,
    val compliance: Boolean?,
    val errorMessages: Boolean?
)
