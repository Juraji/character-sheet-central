package nl.juraji.charactersheetscentral.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.jackson2.CoreJackson2Module
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.security.web.jackson2.WebJackson2Module
import org.springframework.security.web.jackson2.WebServletJackson2Module
import org.springframework.security.web.server.jackson2.WebServerJackson2Module

@Configuration
class JacksonObjectMapperConfiguration {
    @Bean
    @Primary
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder = Jackson2ObjectMapperBuilder()
        .modules(KotlinModule.Builder().build())
        .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .serializers(InstantSerializer.INSTANCE)
        .deserializers(InstantDeserializer.INSTANT)
        .featuresToEnable(
            MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS,
            SerializationFeature.WRITE_DATES_WITH_ZONE_ID,
            DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
        )
        .featuresToDisable(
            SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,
            DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS
        )

    @Bean
    @Primary
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper = builder.build()

    @Bean
    fun oauth2objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        return builder
            .build<ObjectMapper>()
            .registerModules(
                CoreJackson2Module(),
                WebJackson2Module(),
                WebServerJackson2Module(),
                WebServletJackson2Module(),
                JavaTimeModule(),
                OAuth2AuthorizationServerJackson2Module(),
            )
    }
}
