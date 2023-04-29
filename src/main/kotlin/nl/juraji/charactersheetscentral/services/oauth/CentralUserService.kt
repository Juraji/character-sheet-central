package nl.juraji.charactersheetscentral.services.oauth

import nl.juraji.charactersheetscentral.configuration.CouchCbConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindOperationResult
import nl.juraji.charactersheetscentral.couchcb.find.FindQuery
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass

@Repository
class CentralUserService(
    private val configuration: CouchCbConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralUser>(couchDb), UserDetailsService {
    override val databaseName: String
        get() = configuration.authorizationsDatabaseName

    override val documentClass: KClass<CentralUser>
        get() = CentralUser::class

    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindOperationResult<CentralUser>> by lazy {
        object : ParameterizedTypeReference<ApiFindOperationResult<CentralUser>>() {}
    }

    override fun loadUserByUsername(username: String): UserDetails =
        FindQuery("username" to username)
            .let(::findOneDocumentBySelector)
            ?.toUserDetails()
            ?: throw UsernameNotFoundException("User \"$username\" not found!")

    private fun CentralUser.toUserDetails(): UserDetails = User
        .withUsername(username)
        .password(password)
        .disabled(disabled)
        .accountExpired(accountExpired)
        .accountLocked(accountLocked)
        .credentialsExpired(credentialsExpired)
        .authorities(authorities)
        .build()

}
