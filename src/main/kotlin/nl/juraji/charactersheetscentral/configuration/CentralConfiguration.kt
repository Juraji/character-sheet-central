package nl.juraji.charactersheetscentral.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("cs-central")
data class CentralConfiguration(
    val rootDbName: String,
    val userDbPrefix: String,
)
