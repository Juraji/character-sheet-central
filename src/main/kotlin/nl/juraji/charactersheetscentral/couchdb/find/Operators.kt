package nl.juraji.charactersheetscentral.couchdb.find

// Match operators
internal object Operators {
    /** Match fields "less than" this one. */
    const val LT = "\$lt"

    /** Match fields "greater than" this one. */
    const val GT = "\$gt"

    /** Match fields "less than or equal to" this one. */
    const val LTE = "\$lte"

    /** Match fields "greater than or equal to" this one. */
    const val GTE = "\$gte"

    /** Match fields equal to this one. */
    const val EQ = "\$eq"

    /** Match fields not equal to this one. */
    const val NE = "\$ne"

    /** True if the field should exist, false otherwise. */
    const val EXISTS = "\$exists"

    /** One of: "null", "boolean", "number", "string", "array", or "object". */
    const val TYPE = "\$type"

    /** The document field must exist in the list provided. */
    const val IN = "\$in"

    /** The document field must not exist in the list provided. */
    const val NIN = "\$nin"

    /** Special condition to match the length of an array field in a document. Non-array fields cannot match this condition. */
    const val SIZE = "\$size"

    /**
     * Divisor and Remainder are both positive or negative integers.
     * Non-integer values result in a 404 status.
     * Matches documents where (field % Divisor == Remainder) is true, and only when the document field is an integer.
     * [divisor, remainder]
     */
    const val MOD = "\$mod"

    /** A regular expression pattern to match against the document field. Only matches when the field is a string value and matches the supplied regular expression. */
    const val REGEX = "\$regex"

    /** Matches an array value if it contains all the elements of the argument array. */
    const val ALL = "\$all"

    /** Matches and returns all documents that contain an array field with at least one element that matches all the specified query criteria. */
    const val ELEM_MATCH = "\$elemMatch"

    /** Matches and returns all documents that contain an array field with all its elements matching all the specified query criteria. */
    const val ALL_MATCH = "\$allMatch"

    /** Matches and returns all documents that contain a map that contains at least one key that matches all the specified query criteria. */
    const val KEY_MAP_MATCH = "\$keyMapMatch"

    /** Matches if all the selectors in the array match. */
    const val AND = "\$and"

    /** Matches if any of the selectors in the array match. All selectors must use the same index. */
    const val OR = "\$or"

    /** Matches if the given selector does not match. */
    const val NOT = "\$not"

    /** Matches if none of the selectors in the array match. */
    const val NOR = "\$nor"
}
