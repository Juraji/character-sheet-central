package nl.juraji.charactersheetscentral.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class LoginController {

    @RequestMapping("/login")
    fun login(): String = "login"
}
