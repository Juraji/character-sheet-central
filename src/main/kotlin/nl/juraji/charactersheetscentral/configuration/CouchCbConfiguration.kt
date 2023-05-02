package nl.juraji.charactersheetscentral.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import nl.juraji.charactersheetscentral.couchdb.support.CouchDbRestTemplateErrorHandler
import nl.juraji.charactersheetscentral.couchdb.support.CouchDbRestTemplateNotFoundInterceptor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@ConfigurationProperties("couchdb")
data class CouchCbConfiguration(
    val url: String,
    val username: String,
    val password: String,
) {

    @Bean
    fun couchDbRestTemplate(
        objectMapper: ObjectMapper,
        errorHandler: CouchDbRestTemplateErrorHandler,
        interceptor: CouchDbRestTemplateNotFoundInterceptor
    ): RestTemplate {
        return RestTemplateBuilder()
            .rootUri(url)
            .basicAuthentication(username, password)
            .errorHandler(errorHandler)
            .interceptors(interceptor)
            .messageConverters(MappingJackson2HttpMessageConverter(objectMapper))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .build()
    }
}
