package polyflow.features.project.model.response

import polyflow.features.project.model.result.ProjectFeatures

data class ProjectFeaturesResponse(
    val gasStation: Boolean,
    val networkSwitcher: Boolean,
    val connectWallet: Boolean,
    val compliance: Boolean,
    val errorMessages: Boolean
) {
    constructor(projectFeatures: ProjectFeatures) : this(
        gasStation = projectFeatures.gasStation,
        networkSwitcher = projectFeatures.networkSwitcher,
        connectWallet = projectFeatures.connectWallet,
        compliance = projectFeatures.compliance,
        errorMessages = projectFeatures.errorMessages
    )
}
