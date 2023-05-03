package nl.juraji.charactersheetscentral.util

import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

abstract class LoggerCompanion(loggerCls: KClass<*>) {
    val LOGGER = LoggerFactory.getLogger(loggerCls.java)
}
