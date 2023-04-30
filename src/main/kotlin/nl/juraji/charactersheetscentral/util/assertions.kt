package nl.juraji.charactersheetscentral.util

import kotlin.contracts.contract

fun assertNull(value: Any?, lazyMessage: () -> String = { "Null assertion error" }) {
    contract {
        returns() implies (value == null)
    }

    if (value != null) throw IllegalArgumentException(lazyMessage())
}

fun <T : Any> assertNotNull(value: T?, lazyMessage: () -> String = { "Not null assertion error" }): T {
    contract {
        returns() implies (value != null)
    }

    return value ?: throw IllegalArgumentException(lazyMessage())
}

