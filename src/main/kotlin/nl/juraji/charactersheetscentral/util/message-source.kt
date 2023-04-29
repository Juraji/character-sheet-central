package nl.juraji.charactersheetscentral.util

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

fun MessageSource.localizedMessage(code: String, vararg args: Any): String =
    getMessage(code, args, LocaleContextHolder.getLocale())
