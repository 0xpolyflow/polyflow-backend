package polyflow.features.project.model.result

import polyflow.generated.jooq.id.ProjectFeaturesId

data class ProjectFeatures(
    val id: ProjectFeaturesId,
    val gasStation: Boolean,
    val networkSwitcher: Boolean,
    val connectWallet: Boolean,
    val compliance: Boolean,
    val errorMessages: Boolean
)
