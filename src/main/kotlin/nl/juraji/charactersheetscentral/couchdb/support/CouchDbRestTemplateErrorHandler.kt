package nl.juraji.charactersheetscentral.couchdb.support

import com.fasterxml.jackson.databind.ObjectMapper
import nl.juraji.charactersheetscentral.util.assertNotNull
import nl.juraji.charactersheetscentral.util.l
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler

@Component
class CouchDbRestTemplateErrorHandler(
    private val messages: MessageSource,
    private val objectMapper: ObjectMapper,
) : ResponseErrorHandler {
    override fun hasError(response: ClientHttpResponse): Boolean =
        response.statusCode.run { is4xxClientError || is5xxServerError }

    override fun handleError(response: ClientHttpResponse) {
        val statusCode = response.statusCode
        val message = when (statusCode) {
            HttpStatus.NOT_FOUND -> null // Ignore not founds, business logic should handle this
            HttpStatus.BAD_REQUEST -> objectMapper
                .runCatching { readValue(response.body, CouchOperationError::class.java) }
                .map { assertNotNull(it.reason) }
                .map { messages.l("couchDbApi.responses.badRequest", it) }
                .getOrElse { messages.l("couchDbApi.responses.badRequestUnknown") }

            HttpStatus.UNAUTHORIZED -> messages.l("couchDbApi.responses.unauthorized")

            HttpStatus.CONFLICT -> messages.l("couchDbApi.responses.conflict")

            HttpStatus.PRECONDITION_FAILED -> response.body
                .let { objectMapper.readValue(it, CouchOperationError::class.java) }
                .takeIf { it.reason != null }
                ?.let { messages.l("couchDbApi.responses.preconditionFailed", it) }
                ?: messages.l("couchDbApi.responses.preconditionFailedUnknown")

            else -> messages
                .l("couchDbApi.responses.unknown")
                .run { throw CouchDbApiException(statusCode, this) }
        }

        if (message != null) throw CouchDbApiException(statusCode, message)
    }
}
