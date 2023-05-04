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
        model["registrationCodeFieldError"] = false
        model["usernameFieldError"] = false
        model["passwordFieldError"] = false
        model["registrationCode"] = registrationCode
        model["username"] = username

        val registrationCodeDoc = registrationCodeService.findRegistrationCode(registrationCode)

        if (registrationCodeDoc == null) {
            model["registrationCodeFieldError"] = true
            return "signup"
        }

        if (userService.userExists(username)) {
            model["usernameFieldError"] = true
            return "signup"
        }

        if (password != passwordRepeat) {
            model["passwordFieldError"] = true
            return "signup"
        }

        // Everything seems to be in order, create user and delete registration code
        val userResult = userService
            .runCatching {
                createUser(
                    username,
                    password,
                    CentralUserRole.MEMBER,
                    CentralUserRole.COUCH_DB_ACCESS,
                    CentralUserRole.INBOXES_SEND
                )
            }
            .onSuccess { registrationCodeService.delete(registrationCodeDoc) }

        return if (userResult.isSuccess) {
            "redirect:login?signUpComplete&username=$username"
        } else {
            model["usernameFieldError"] = true
            "signup"
        }
    }
}
