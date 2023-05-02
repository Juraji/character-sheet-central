package nl.juraji.charactersheetscentral.couchdb.support

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.InputStream

@Component
class CouchDbRestTemplateNotFoundInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val response = execution.execute(request, body)

        return if (response.statusCode == HttpStatus.NOT_FOUND) copyAsEmpty(response)
        else response
    }

    private fun copyAsEmpty(response: ClientHttpResponse): ClientHttpResponse = object : ClientHttpResponse {
        override fun getHeaders(): HttpHeaders = response.headers

        override fun getBody(): InputStream = ByteArrayInputStream(byteArrayOf())

        override fun close() = response.close()

        override fun getStatusCode(): HttpStatusCode = response.statusCode

        @Suppress("DEPRECATION")
        @Deprecated("Deprecated in Java", replaceWith = ReplaceWith(""))
        override fun getRawStatusCode(): Int = response.rawStatusCode

        override fun getStatusText(): String = response.statusText
    }
}
