package nl.juraji.charactersheetscentral.couchdb.documents

interface CentralDocument : CouchDbDocument {
    val modelType: String get() = this::class.simpleName!!
}

