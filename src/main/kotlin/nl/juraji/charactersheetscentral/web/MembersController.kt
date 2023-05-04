package nl.juraji.charactersheetscentral.web

import nl.juraji.charactersheetscentral.services.oauth.CentralOAuth2AuthorizationConsentService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class MembersController(
    private val consentService: CentralOAuth2AuthorizationConsentService,
) {

    @RequestMapping("/members")
    fun memberUserHome(
        @AuthenticationPrincipal userDetails: UserDetails,
        model: Model
    ): String {
        model["appsWithConsent"] = consentService
            .findAllByPrincipal(userDetails.username)

        return "members"
    }
}
