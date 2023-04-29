package nl.juraji.charactersheetscentral.couchcb.find

import nl.juraji.charactersheetscentral.couchcb.support.DocumentIdMeta

data class ApiFindOperationResult<T : DocumentIdMeta>(
    val docs: List<T>,
)
