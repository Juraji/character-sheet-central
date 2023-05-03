package nl.juraji.charactersheetscentral.services.users

object CentralUserRole {
    const val ADMIN = "ADMIN" // Server administrators
    const val MEMBER = "MEMBER" // Server member
    const val COUCH_DB_ACCESS = "COUCH_DB_ACCESS" // Can acces OWN instance of Couch DB
    const val USE_INBOXES = "USE_INBOXES" // Can use the inbox outbox system
}
