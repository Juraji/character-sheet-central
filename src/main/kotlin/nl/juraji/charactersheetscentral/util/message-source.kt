package nl.juraji.charactersheetscentral.util

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * Wrapper for [MessageSource.getMessage], use the locale from the [LocaleContextHolder].
 * The function name "l" stands for localized, but is kept short, since the i18n keys are already very long.
 */
fun MessageSource.l(code: String, vararg args: Any): String =
    getMessage(code, args, LocaleContextHolder.getLocale())
