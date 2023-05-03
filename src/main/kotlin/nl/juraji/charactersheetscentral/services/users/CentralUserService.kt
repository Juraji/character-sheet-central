package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.configuration.CentralConfiguration
import nl.juraji.charactersheetscentral.couchdb.CouchDbService
import nl.juraji.charactersheetscentral.couchdb.DocumentRepository
import nl.juraji.charactersheetscentral.couchdb.find.*
import nl.juraji.charactersheetscentral.couchdb.indexes.CreateIndexOp
import nl.juraji.charactersheetscentral.couchdb.indexes.Index
import nl.juraji.charactersheetscentral.couchdb.indexes.partialFilterSelector
import nl.juraji.charactersheetscentral.util.assertFalse
import nl.juraji.charactersheetscentral.util.jackson.restTemplateTypeRef
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.core.authority.SimpleGrantedAuthority
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
    private val couchDb: CouchDbService,
    private val configuration: CentralConfiguration,
) : DocumentRepository<CentralUser>(couchDb), UserDetailsManager, UserDetailsPasswordService {
    override val databaseName: String = configuration.rootDbName

    override val documentClass: KClass<CentralUser> = CentralUser::class

    override val documentFindTypeRef: ParameterizedTypeReference<FindResult<CentralUser>>
        get() = restTemplateTypeRef<FindResult<CentralUser>>()

    fun findAll(): List<CentralUser> = findDocumentsBySelector(queryModel())

    fun findByUsername(username: String): CentralUser =
        findOneDocumentBySelector(usernameSelector(username))
            ?: throw UsernameNotFoundException("User with name $username does not exist!")

    override fun loadUserByUsername(username: String): UserDetails =
        findByUsername(username).run {
            // Specifically do not set a password encoder, as the password is already encoded!
            User.builder()
                .username(username)
                .password(password)
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

        couchDb.createDatabase(configuration.userDbPrefix + username)
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
//        val currentUsername: String =
//            SecurityContextHolder.getContext()?.authentication?.name
//                ?: throw AccessDeniedException("No authenticated user")

        TODO("Not yet implemented")
    }

    override fun updatePassword(user: UserDetails, newPassword: String): UserDetails {
        TODO("Not yet implemented")
    }

    override fun userExists(username: String): Boolean =
        documentExistsBySelector(usernameSelector(username))

    override fun defineIndexes(): List<CreateIndexOp> = listOf(
        CreateIndexOp(
            name = USERNAME_IDX,
            index = Index(
                fields = setOf("username"),
                partialFilterSelector = partialFilterSelector(CentralUser::class)
            )
        )
    )

    private fun usernameSelector(username: String): FindQuery<CentralUser> =
        query<CentralUser>(eq("username", username.lowercase())).usingIndex(USERNAME_IDX)

    companion object {
        const val USERNAME_IDX = "idx__centralUser__username"
    }
}
