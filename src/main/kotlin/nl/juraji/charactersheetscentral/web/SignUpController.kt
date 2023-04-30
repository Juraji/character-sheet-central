package nl.juraji.charactersheetscentral.web

import nl.juraji.charactersheetscentral.services.users.CentralRegistrationCodeService
import nl.juraji.charactersheetscentral.services.users.CentralUserRole
import nl.juraji.charactersheetscentral.services.users.CentralUserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SignUpController(
    private val userService: CentralUserService,
    private val registrationCodeService: CentralRegistrationCodeService,
) {

    @GetMapping("/signup")
    fun signUp(model: Model): String {
        model["fieldErrors"] = emptyMap<String, String>()
        model["registrationCode"] = ""
        model["username"] = ""
        return "signup"
    }

    @PostMapping("/signup")
    fun processSignUp(
        model: Model,
        @RequestParam registrationCode: String,
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam passwordRepeat: String,
    ): String {
        val fieldErrors: MutableList<String> = mutableListOf()
        model["fieldErrors"] = fieldErrors
        model["registrationCode"] = registrationCode
        model["username"] = username

        val regCode = registrationCodeService.findRegistrationCode(registrationCode)

        if (regCode == null) {
            fieldErrors.add("registrationCode")
            return "signup"
        }

        if (userService.userExists(username)) {
            fieldErrors.add("username")
            return "signup"
        }

        if (password !== passwordRepeat) {
            fieldErrors.add("password")
            fieldErrors.add("passwordRepeat")
            return "signup"
        }

        userService.createUser(username, password, CentralUserRole.MEMBER)

        // Everything is ok!
        return "redirect:login?signUpComplete&username=$username"
    }
}
