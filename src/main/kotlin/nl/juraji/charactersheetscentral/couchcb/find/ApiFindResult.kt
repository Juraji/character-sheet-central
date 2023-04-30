package nl.juraji.charactersheetscentral.couchcb.find

import nl.juraji.charactersheetscentral.couchcb.support.DocumentIdMeta

data class ApiFindResult<T : DocumentIdMeta>(
    val docs: List<T>,
)
