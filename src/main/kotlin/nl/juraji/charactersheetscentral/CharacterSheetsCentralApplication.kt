package nl.juraji.charactersheetscentral

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan("nl.juraji.charactersheetscentral.configuration")
class CharacterSheetsCentralApplication

fun main(args: Array<String>) {
    runApplication<CharacterSheetsCentralApplication>(*args)
}
