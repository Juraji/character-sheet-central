package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.configuration.CouchCbConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
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

    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<CentralUser>> by lazy {
        object : ParameterizedTypeReference<ApiFindResult<CentralUser>>() {}
    }

    override fun loadUserByUsername(username: String): UserDetails =
        DocumentSelector("username" to username)
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
