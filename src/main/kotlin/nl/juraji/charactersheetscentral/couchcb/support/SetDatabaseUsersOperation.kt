package nl.juraji.charactersheetscentral.couchcb.support

data class DatabaseUsers(
    val names: Set<String>,
    val roles: Set<String> = emptySet()
)

data class SetDatabaseUsersOperation(
    val admins: DatabaseUsers,
    val members: DatabaseUsers,
)
