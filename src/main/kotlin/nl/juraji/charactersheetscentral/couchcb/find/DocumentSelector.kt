package nl.juraji.charactersheetscentral.couchcb.find

data class DocumentSelector(
    val selector: Map<String, Any>,
    val sort: List<Map<String, Any>> = emptyList(),
    val limit: Int = 25,
    val skip: Int = 0
) {
    constructor(vararg selector: Pair<String, Any>) : this(mapOf(*selector))

    fun singleResult(): DocumentSelector = this.copy(limit = 1, skip = 0)
}
