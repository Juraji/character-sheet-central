package nl.juraji.charactersheetscentral.services.users

import nl.juraji.charactersheetscentral.couchdb.documents.CentralDocument
import java.time.Instant

data class CentralRegistrationCode(
    override val id: String? = null,
    override val rev: String? = null,
    val name: String,
    val code: String,
    val expiresAt: Instant
) : CentralDocument
