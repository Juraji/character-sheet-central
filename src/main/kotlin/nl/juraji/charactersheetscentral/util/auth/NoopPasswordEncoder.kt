package nl.juraji.charactersheetscentral.util.auth

import org.springframework.security.crypto.password.PasswordEncoder

class NoopPasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: CharSequence): String =
        throw IllegalStateException("This encoder should not be used for password encoding!")

    override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean =
        rawPassword == encodedPassword
}
