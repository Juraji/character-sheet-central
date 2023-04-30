package nl.juraji.charactersheetscentral.util

import kotlin.contracts.contract

fun assertNull(value: Any?, lazyMessage: () -> String = { "Null assertion error" }) {
    contract {
        returns() implies (value == null)
    }

    if (value != null) throw AssertionException(lazyMessage)
}

fun <T : Any> assertNotNull(value: T?, lazyMessage: () -> String = { "Not null assertion error" }): T {
    contract {
        returns() implies (value != null)
    }

    return value ?: throw AssertionException(lazyMessage)
}

fun assertTrue(condition: Boolean, lazyMessage: () -> String = { "True assertion error" }) {
    if (!condition) throw AssertionException(lazyMessage)
}

fun assertFalse(condition: Boolean, lazyMessage: () -> String = { "False assertion error" }) {
    if (condition) throw AssertionException(lazyMessage)
}

class AssertionException(message: String) : Exception(message) {
    constructor(lazyMessage: () -> String) : this(lazyMessage())
}
