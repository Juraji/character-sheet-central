package nl.juraji.charactersheetscentral.util

infix fun <A, B, C> Pair<A, B>.then(third: C): Triple<A, B, C> = Triple(first, second, third)
