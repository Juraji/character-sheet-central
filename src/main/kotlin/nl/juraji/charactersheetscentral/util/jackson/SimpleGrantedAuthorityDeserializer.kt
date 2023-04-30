package nl.juraji.charactersheetscentral.util.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class GrantedAuthorityListDeserializer : JsonDeserializer<List<GrantedAuthority>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<GrantedAuthority> {
        val node: JsonNode = p.codec.readTree(p)
        val authorities = mutableListOf<GrantedAuthority>()
        node.forEach {
            val authority = SimpleGrantedAuthority(it.get("authority").asText())
            authorities.add(authority)
        }
        return authorities
    }
}
