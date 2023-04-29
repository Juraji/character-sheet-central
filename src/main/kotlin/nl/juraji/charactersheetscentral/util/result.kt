package nl.juraji.charactersheetscentral.util

/**
 * When [this] is a failure, the given exception is rethrown, unless the [catchCondition] predicate returns true.
 * Note, the success result is voided when using this function!
 */
fun Result<*>.catchOrThrow(catchCondition: (Throwable) -> Boolean = { true }) {
    val ex = exceptionOrNull()
    when {
        ex == null || catchCondition(ex) -> return
        else -> throw ex
    }
}
