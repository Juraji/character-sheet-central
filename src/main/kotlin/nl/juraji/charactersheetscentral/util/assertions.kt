package nl.juraji.charactersheetscentral.util

import kotlin.contracts.contract

fun assertNull(value: Any?, lazyMessage: () -> String = { "Null assertion error" }) {
    contract {
        returns() implies (value == null)
    }

    if (value != null) throw IllegalArgumentException(lazyMessage())
}

fun assertNotNull(value: Any?, lazyMessage: () -> String = { "Not null assertion error" }) {
    contract {
        returns() implies (value != null)
    }

    if (value == null) throw IllegalArgumentException(lazyMessage())
}

