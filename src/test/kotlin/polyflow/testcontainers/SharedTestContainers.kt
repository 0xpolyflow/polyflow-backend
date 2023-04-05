package polyflow.testcontainers

object SharedTestContainers {
    val postgresContainer by lazy { PostgresTestContainer() }
    val hardhatContainer by lazy { HardhatTestContainer() }
}
