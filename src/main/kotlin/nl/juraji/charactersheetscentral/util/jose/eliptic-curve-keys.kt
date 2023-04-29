package nl.juraji.charactersheetscentral.util.jose

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
import java.util.*

fun generateEcKey(): KeyPair {
    val ellipticCurve = EllipticCurve(
        ECFieldFp(BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951")),
        BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948"),
        BigInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291")
    )
    val ecPoint = ECPoint(
        BigInteger("48439561293906451759052585252797914202762949526041747995844080717082404635286"),
        BigInteger("36134250956749795798585127919587881956611106672985015071877198253568414405109")
    )
    val ecParameterSpec = ECParameterSpec(
        ellipticCurve,
        ecPoint,
        BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369"),
        1
    )

    return KeyPairGenerator.getInstance("EC")
        .apply { initialize(ecParameterSpec) }
        .generateKeyPair()
}

fun generateEc(): ECKey = generateEcKey().run {
    val keyId = UUID.randomUUID().toString()
    val pubKey = public as ECPublicKey
    val priKey = private as ECPrivateKey
    val curve = Curve.forECParameterSpec(pubKey.params)

    ECKey.Builder(curve, pubKey)
        .privateKey(priKey)
        .keyID(keyId)
        .build()
}
