package nl.juraji.charactersheetscentral.couchdb.support

import org.springframework.http.HttpStatusCode

data class CouchDbApiException(
    val httpStatus: HttpStatusCode,
    override val message: String
) : Exception("code ${httpStatus.value()}: $message")
