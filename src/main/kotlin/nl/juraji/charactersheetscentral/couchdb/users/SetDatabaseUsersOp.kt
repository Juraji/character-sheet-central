package nl.juraji.charactersheetscentral.couchdb.users

data class Users(
    val names: Set<String>,
    val roles: Set<String> = emptySet()
)

data class SetDatabaseUsersOp(
    val admins: Users,
    val members: Users,
)
