package nl.juraji.charactersheetscentral.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import nl.juraji.charactersheetscentral.services.oauth.CentralScopes
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Duration

@Configuration
class RegisteredClientsConfiguration {
    @Bean
    fun registeredClientRepository(
        objectMapper: ObjectMapper,
        @Value("classpath:clients.json") clientsResource: Resource
    ): RegisteredClientRepository {
        val clients = objectMapper
            .readValue(clientsResource.inputStream, jacksonTypeRef<List<Map<String, Any>>>())
            .map(::configureClient)

        return InMemoryRegisteredClientRepository(clients)
    }

    private fun configureClient(c: Map<String, Any>): RegisteredClient = RegisteredClient
        .withId(c["id"] as String)
        .clientId(c["clientId"] as String)
        .clientName(c["clientName"] as String)
        .clientSecret(c["clientSecret"] as String)
        .redirectUri(c["redirectUri"] as String)
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope(OidcScopes.OPENID)
        .scope(OidcScopes.PROFILE)
        .scope(CentralScopes.COUCHDB_SYNC)
        .clientSettings(
            ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build()
        )
        .tokenSettings(
            TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes((c["accessTokenTimeToLive"] as Int).toLong()))
                .refreshTokenTimeToLive(Duration.ofMinutes((c["refreshTokenTimeToLive"] as Int).toLong()))
                .build()
        )
        .build()
}
