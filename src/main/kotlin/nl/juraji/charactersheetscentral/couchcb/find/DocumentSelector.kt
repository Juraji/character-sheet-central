package nl.juraji.charactersheetscentral.couchcb.find

import nl.juraji.charactersheetscentral.couchcb.support.CentralDocument

data class DocumentSelector<T : CentralDocument>(
    val selector: Map<String, Any>,
    val sort: List<Map<String, Any>>? = null,
    val limit: Int = 25,
    val skip: Int = 0,
    val fields: Set<String>? = null
) {
    /**
     * Map selector to match only a single document
     */
    fun singleResult(): DocumentSelector<T> = this.copy(limit = 1, skip = 0)

    /**
     * Include only specific fields in the result documents.
     * Note: If [includeSelected] is true (default), the root fields in the selector are also included in the fields
     * to make it so CouchDB can use the most appropriate index
     */
    fun withFields(vararg fields: String, includeSelected: Boolean = true): DocumentSelector<T> {
        val selectFields = if (includeSelected) fields.toSet() + selector.keys else fields.toSet()
        return this.copy(fields = selectFields)
    }

    @Suppress("unused")
    companion object {
        inline fun <reified T : CentralDocument> select(vararg selector: Pair<String, Any>): DocumentSelector<T> =
            DocumentSelector(mapOf("modelType" to T::class.simpleName!!, *selector))

        // Combination operators
        /** Matches if all the selectors in the array match. */
        const val OP_AND = "\$and"

        /** Matches if any of the selectors in the array match. All selectors must use the same index. */
        const val OP_OR = "\$or"

        /** Matches if the given selector does not match. */
        const val OP_NOT = "\$not"

        /** Matches if none of the selectors in the array match. */
        const val OP_NOR = "\$nor"

        // Equality operators
        /** Match fields "less than" this one. */
        const val OP_LT = "\$lt"

        /** Match fields "greater than" this one. */
        const val OP_GT = "\$gt"

        /** Match fields "less than or equal to" this one. */
        const val OP_LTE = "\$lte"

        /** Match fields "greater than or equal to" this one. */
        const val OP_GTE = "\$gte"

        /** Match fields equal to this one. */
        const val OP_EQ = "\$eq"

        /** Match fields not equal to this one. */
        const val OP_NE = "\$ne"

        /** True if the field should exist, false otherwise. */
        const val OP_EXISTS = "\$exists"

        /** One of: "null", "boolean", "number", "string", "array", or "object". */
        const val OP_TYPE = "\$type"

        /** The document field must exist in the list provided. */
        const val OP_IN = "\$in"

        /** The document field must not exist in the list provided. */
        const val OP_NIN = "\$nin"

        /** Special condition to match the length of an array field in a document. Non-array fields cannot match this condition. */
        const val OP_SIZE = "\$size"

        /**
         * Divisor and Remainder are both positive or negative integers.
         * Non-integer values result in a 404 status.
         * Matches documents where (field % Divisor == Remainder) is true, and only when the document field is an integer.
         * [divisor, remainder]
         */
        const val OP_MOD = "\$mod"

        /** A regular expression pattern to match against the document field. Only matches when the field is a string value and matches the supplied regular expression. */
        const val OP_REGEX = "\$regex"

        /** Matches an array value if it contains all the elements of the argument array. */
        const val OP_ALL = "\$all"

        const val OP_ELEMMATCH = "\$elemMatch"
    }
}
