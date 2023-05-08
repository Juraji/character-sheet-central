package nl.juraji.charactersheetscentral.web

import nl.juraji.charactersheetscentral.services.oauth.CentralOAuthClientService
import nl.juraji.charactersheetscentral.services.users.CentralRegistrationCodeService
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AdminController(
    private val registrationCodeService: CentralRegistrationCodeService,
    private val oAuthClientService: CentralOAuthClientService,
    private val passwordEncoder: PasswordEncoder,
) {

    @RequestMapping("/admin")
    @PostAuthorize("hasRole(T(nl.juraji.charactersheetscentral.services.users.CentralUserRole).ADMIN)")
    fun adminPanel(model: Model): String {
        model["unclaimedRegistrationCodes"] = registrationCodeService.findAllUnclaimed()
        model["oAuthClients"] = oAuthClientService.findAll()

        return "admin"
    }

    @RequestMapping("/admin/registration-codes/create")
    @PostAuthorize("hasRole(T(nl.juraji.charactersheetscentral.services.users.CentralUserRole).ADMIN)")
    fun createRegistrationCode(
        @RequestParam name: String,
    ): String = registrationCodeService.run {
        val r = createRegistrationCode(name)
        "redirect:/admin?message=Registration code generated with name \"${r.name}\": \"${r.code}\"!"
    }

    @RequestMapping("/admin/registration-codes/delete")
    @PostAuthorize("hasRole(T(nl.juraji.charactersheetscentral.services.users.CentralUserRole).ADMIN)")
    fun deleteRegistrationCode(
        @RequestParam id: String,
    ): String = registrationCodeService.run {
        findDocumentById(id)?.let { delete(it) }
        "redirect:/admin?message=Registration code deleted!"
    }

    @RequestMapping("/admin/oauth-clients/create")
    @PostAuthorize("hasRole(T(nl.juraji.charactersheetscentral.services.users.CentralUserRole).ADMIN)")
    fun createOAuthClient(
        @RequestParam clientId: String,
    ): String = oAuthClientService.run {
        val client = defaultClient()
            .copy(clientId = clientId, clientName = clientId, clientSecret = passwordEncoder.encode(clientId))

        saveDocument(client)
        "redirect:/admin?message=OAuth client created with secret: ${client.clientSecret}!"
    }
}
