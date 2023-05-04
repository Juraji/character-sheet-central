package nl.juraji.charactersheetscentral.web

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class LoginController {

    @RequestMapping("/login")
    fun login(
        @AuthenticationPrincipal userDetails: UserDetails?,
    ): String {
        if (userDetails != null) {
            return "redirect:/members"
        }

        return "login"
    }
}
