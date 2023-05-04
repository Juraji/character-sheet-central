package nl.juraji.charactersheetscentral.services.users

/**
 * Central user roles
 * The _AUTHORITY consts are here because the User impl of Spring appends ROLE_ to all role authorities,
 * but does not expose a constant for it...
 */
object CentralUserRole {
    const val AUTHORITY_PREFIX = "ROLE_"

    const val ADMIN = "ADMIN" // Server administrators
    const val MEMBER = "MEMBER" // Server member
    const val COUCH_DB_ACCESS = "COUCH_DB_ACCESS" // Can acces OWN instance of Couch DB
    const val INBOXES_SEND = "INBOXES_SEND" // Can share items via inboxes

    fun authorityOf(role: String): String = authorityOf { role }
    fun authorityOf(block: CentralUserRole.() -> String): String =
        block.invoke(this).commonPrefixWith(AUTHORITY_PREFIX)
}
