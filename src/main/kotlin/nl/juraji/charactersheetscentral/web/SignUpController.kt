package nl.juraji.charactersheetscentral.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SignUpController {

    @GetMapping("/signup")
    fun signUp() = "signup"

    @PostMapping("/signup")
    fun processSignUp(
        model: Model,
        @RequestParam registrationCode: String,
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam passwordRepeat: String,
    ): String {
        // TODO: Implement validating and creating the user
        // Everything is ok!
        return "redirect:/login?signUpComplete&username=$username"
    }


}
