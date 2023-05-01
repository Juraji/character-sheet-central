package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchcb.CouchDbDocumentRepository
import nl.juraji.charactersheetscentral.couchcb.CouchDbService
import nl.juraji.charactersheetscentral.couchcb.find.ApiFindResult
import nl.juraji.charactersheetscentral.couchcb.find.DocumentSelector
import nl.juraji.charactersheetscentral.couchcb.support.CreateIndexOperation
import nl.juraji.charactersheetscentral.couchcb.support.Index
import nl.juraji.charactersheetscentral.util.assertFalse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsPasswordService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Repository
import kotlin.reflect.KClass

@Repository
class CentralUserService(
    private val passwordEncoder: PasswordEncoder,
    configuration: CentralConfiguration,
    couchDb: CouchDbService,
) : CouchDbDocumentRepository<CentralUser>(couchDb), UserDetailsManager, UserDetailsPasswordService {
    override val databaseName: String = configuration.rootDbName

    override val documentClass: KClass<CentralUser> = CentralUser::class

    override val documentFindTypeRef: ParameterizedTypeReference<ApiFindResult<CentralUser>>
        get() = object : ParameterizedTypeReference<ApiFindResult<CentralUser>>() {}

    fun findByUsername(username: String): CentralUser =
        findOneDocumentBySelector(usernameSelector(username))
            ?: throw UsernameNotFoundException("User with name $username does not exist!")

    override fun loadUserByUsername(username: String): UserDetails =
        findByUsername(username).run {
            User.builder()
                .username(username)
                .password(password)
                .passwordEncoder(passwordEncoder::encode)
                .disabled(!enabled)
                .accountExpired(!accountNonExpired)
                .credentialsExpired(!credentialsNonExpired)
                .accountLocked(!accountNonLocked)
                .authorities(authorities.map(::SimpleGrantedAuthority))
                .build()
        }

    fun createUser(username: String, password: String, vararg roles: String) {
        User.builder()
            .username(username)
            .password(password)
            .passwordEncoder(passwordEncoder::encode)
            .roles(*roles)
            .build()
            .let(::createUser)
    }

    override fun createUser(user: UserDetails) {
        assertFalse(userExists(user.username)) { "User with username ${user.username} already exists." }

        user
            .run {
                CentralUser(
                    username = username.lowercase(),
                    password = password,
                    enabled = isEnabled,
                    accountNonExpired = isAccountNonExpired,
                    accountNonLocked = isAccountNonLocked,
                    credentialsNonExpired = isCredentialsNonExpired,
                    authorities = authorities.map { it.authority }.toSet(),
                )
            }
            .let(::saveDocument)
    }

    override fun updateUser(user: UserDetails) {
        findByUsername(username = user.username)
            .copy(
                enabled = user.isEnabled,
                accountNonExpired = user.isAccountNonExpired,
                accountNonLocked = user.isAccountNonLocked,
                credentialsNonExpired = user.isCredentialsNonExpired,
                authorities = user.authorities.map { it.authority }.toSet(),
            )
            .let(::saveDocument)
    }

    override fun deleteUser(username: String) {
        findByUsername(username).let(::deleteDocument)
    }

    /**
     * Current context user only!
     */
    override fun changePassword(oldPassword: String, newPassword: String) {
        val currentUsername: String =
            SecurityContextHolder.getContext()?.authentication?.name
                ?: throw AccessDeniedException("No authenticated user")

        TODO("Not yet implemented")
    }

    override fun updatePassword(user: UserDetails, newPassword: String): UserDetails {
        TODO("Not yet implemented")
    }

    override fun userExists(username: String): Boolean =
        documentExistsBySelector(usernameSelector(username))

    override fun defineIndexes(): List<CreateIndexOperation> = listOf(
        CreateIndexOperation(
            name = USERNAME_IDX,
            index = Index(
                fields = setOf("username"),
                partialFilterSelector = DocumentSelector
                    .partialFilterSelector(CentralUser::class)
            )
        )
    )

    private fun usernameSelector(username: String): DocumentSelector<CentralUser> =
        DocumentSelector
            .select<CentralUser>("username" to username.lowercase())
            .withIndex(USERNAME_IDX)

    companion object {
        const val USERNAME_IDX = "idx__centralUser__username"
    }
}
