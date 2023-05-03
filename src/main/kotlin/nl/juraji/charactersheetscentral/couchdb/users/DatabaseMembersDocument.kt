package nl.juraji.charactersheetscentral.couchdb.users

data class MemberList(
    val names: Set<String> = emptySet(),
    val roles: Set<String> = emptySet()
)

data class DatabaseMembersDocument(
    val admins: MemberList = MemberList(),
    val members: MemberList = MemberList(),
)

fun MemberList.addName(name: String) = copy(names = names + name)
fun DatabaseMembersDocument.addMemberName(name: String) = copy(members = members.addName(name))
