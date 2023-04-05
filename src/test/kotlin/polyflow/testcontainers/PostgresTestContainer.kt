package polyflow.testcontainers

import org.jooq.DSLContext
import org.testcontainers.containers.PostgreSQLContainer
import polyflow.generated.jooq.tables.ProjectFeaturesTable
import polyflow.generated.jooq.tables.ProjectTable
import polyflow.generated.jooq.tables.UserTable

class PostgresTestContainer : PostgreSQLContainer<PostgresTestContainer>("postgres:13.4-alpine") {

    init {
        start()
        System.setProperty("POSTGRES_PORT", getMappedPort(POSTGRESQL_PORT).toString())
    }

    fun cleanAllDatabaseTables(dslContext: DSLContext) {
        dslContext.apply {
            deleteFrom(ProjectTable).execute()
            deleteFrom(ProjectFeaturesTable).execute()
            deleteFrom(UserTable).execute()
        }
    }
}
