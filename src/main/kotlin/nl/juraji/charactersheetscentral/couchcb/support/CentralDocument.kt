package nl.juraji.charactersheetscentral.couchcb.support

interface CentralDocument : DocumentIdMeta {
    val modelType: String get() = this::class.simpleName!!
}

data class CentralDocumentMetaData(
    override val id: String?,
    override val rev: String?,
    override val modelType: String = "UNKNOWN",
) : CentralDocument
