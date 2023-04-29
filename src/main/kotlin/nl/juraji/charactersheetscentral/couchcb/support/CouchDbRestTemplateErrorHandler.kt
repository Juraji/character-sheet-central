package nl.juraji.charactersheetscentral.couchcb.support

import com.fasterxml.jackson.databind.ObjectMapper
import nl.juraji.charactersheetscentral.couchcb.CouchDbApiException
import nl.juraji.charactersheetscentral.util.localizedMessage
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler

@Component
class CouchDbRestTemplateErrorHandler(
    private val messageSource: MessageSource,
    private val objectMapper: ObjectMapper,
) : ResponseErrorHandler {
    override fun hasError(response: ClientHttpResponse): Boolean =
        response.statusCode.run { is4xxClientError || is5xxServerError }

    override fun handleError(response: ClientHttpResponse) {
        when (val statusCode = response.statusCode) {
            HttpStatus.BAD_REQUEST -> {
                // @formatter:off
                val message: String = response.body
                    .let { objectMapper.readValue(it, ApiOperationError::class.java) }
                    .takeIf { it.reason != null }
                    ?.let { messageSource.localizedMessage("nl.juraji.CouchDbService.couchApiResponse.badRequest", it) }
                    ?: messageSource.localizedMessage("nl.juraji.CouchDbService.couchApiResponse.badRequestReasonUnknown")
                // @formatter:on

                throw CouchDbApiException(statusCode, message)
            }

            HttpStatus.UNAUTHORIZED -> messageSource
                .localizedMessage("nl.juraji.CouchDbService.couchApiResponse.unauthorized")
                .run { throw CouchDbApiException(statusCode, this) }

            HttpStatus.CONFLICT -> messageSource
                .localizedMessage("nl.juraji.CouchDbService.couchApiResponse.conflict")
                .run { throw CouchDbApiException(statusCode, this) }

            HttpStatus.PRECONDITION_FAILED -> {
                // @formatter:off
                val message: String = response.body
                    .let { objectMapper.readValue(it, ApiOperationError::class.java) }
                    ?.let { messageSource.localizedMessage("nl.juraji.CouchDbService.couchApiResponse.preconditionFailed", it) }
                    ?: messageSource.localizedMessage("nl.juraji.CouchDbService.couchApiResponse.preconditionFailedReasonUnknown")
                // @formatter:on

                throw CouchDbApiException(statusCode, message)
            }

            else -> messageSource
                .localizedMessage("nl.juraji.CouchDbService.couchApiResponse.unknown")
                .run { throw CouchDbApiException(statusCode, this) }
        }
    }
}
