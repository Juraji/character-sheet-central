package nl.juraji.charactersheetscentral.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("cs-central")
data class CentralConfiguration(
    val rootDbName: String,
    val userDbPrefix: String,
    val adminUsername: String,
    val adminPassword: String,
    val indexDesignDocument: String,
) {
    fun userDbName(username: String): String  = userDbPrefix + username.lowercase()
}
