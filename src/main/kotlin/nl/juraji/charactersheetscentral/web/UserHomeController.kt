package nl.juraji.charactersheetscentral.web

import nl.juraji.charactersheetscentral.services.oauth.CentralOAuth2AuthorizationConsentService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class UserHomeController(
    private val consentService: CentralOAuth2AuthorizationConsentService
) {

    @RequestMapping("/user/home")
    fun myApps(
        @AuthenticationPrincipal userDetails: UserDetails,
        model: Model
    ): String {
        model["username"] = userDetails.username
        model["userRole"] = userDetails.authorities.first { it.authority.startsWith("ROLE_") }.authority
        model["appsWithConsent"] = consentService.findAllByPrincipal(userDetails.username)

        return "user-home"
    }
}