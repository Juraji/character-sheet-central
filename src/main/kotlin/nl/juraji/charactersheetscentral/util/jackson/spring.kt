package nl.juraji.charactersheetscentral.util.jackson

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T> restTemplateTypeRef(): ParameterizedTypeReference<T> =
    object : ParameterizedTypeReference<T>() {}
