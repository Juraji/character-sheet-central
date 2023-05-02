package nl.juraji.charactersheetscentral.couchdb.find

enum class FieldType(val typeName: String) {
    NULL("null"),
    BOOLEAN("boolean"),
    NUMBER("number"),
    STRING("string"),
    ARRAY("array"),
    OBJECT("object")
}
