package nl.juraji.charactersheetscentral.web

import nl.juraji.charactersheetscentral.services.users.CentralRegistrationCodeService
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AdminController(
    private val registrationCodeService: CentralRegistrationCodeService,
) {

    @RequestMapping("/admin")
    @PostAuthorize("hasRole(T(nl.juraji.charactersheetscentral.services.users.CentralUserRole).ADMIN)")
    fun adminPanel(model: Model): String {
        model["unclaimedRegistrationCodes"] = registrationCodeService.findAllUnclaimed()

        return "admin"
    }

    @RequestMapping("/admin/registration-codes")
    @PostAuthorize("hasRole(T(nl.juraji.charactersheetscentral.services.users.CentralUserRole).ADMIN)")
    fun handleRegistrationCodes(
        @RequestParam action: String,
        @RequestParam id: String,
        @RequestParam name: String,
    ): String {
        return when (action) {
            "CREATE" -> registrationCodeService.run {
                val r = registrationCodeService.createRegistrationCode(name)
                "redirect:/admin?message=Registration code generated with name \"${r.name}\": \"${r.code}\"!"
            }

            "DELETE" -> registrationCodeService.run {
                findDocumentById(id)?.let { delete(it) }
                "redirect:/admin?message=Registration code deleted!"
            }

            else -> throw IllegalArgumentException("Unknown action $action")
        }
    }
}
