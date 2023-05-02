package nl.juraji.charactersheetscentral.util.jose

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT

fun parseSignedJWT(token: String): SignedJWT = SignedJWT.parse(token)
fun signedJWTClaimSet(token: String): JWTClaimsSet = parseSignedJWT(token).jwtClaimsSet

inline fun <reified T : Any> JWTClaimsSet.claim(name: String): T = getClaim(name) as T
