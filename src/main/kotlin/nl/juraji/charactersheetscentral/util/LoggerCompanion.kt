package nl.juraji.charactersheetscentral.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

abstract class LoggerCompanion(loggerCls: KClass<*>) {
    protected val logger: Logger = LoggerFactory.getLogger(loggerCls.java)
}
