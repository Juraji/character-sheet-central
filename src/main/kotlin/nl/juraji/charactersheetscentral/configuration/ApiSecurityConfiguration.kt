package nl.juraji.charactersheetscentral.configuration

import nl.juraji.charactersheetscentral.util.auth.NoopPasswordEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.session.HttpSessionEventPublisher

@Configuration
class ApiSecurityConfiguration {

    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/assets/**",
                        "/webjars/**",
                        "/login",
                        "/signup",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            // Form login handles the redirect to the login page from the
            // authorization server filter chain
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/user/home")
            }
            .logout { it.permitAll() }
        return http.build()
    }

    @Bean
    fun sessionRegistry(): SessionRegistry = SessionRegistryImpl()

    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher = HttpSessionEventPublisher()

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        (PasswordEncoderFactories.createDelegatingPasswordEncoder() as DelegatingPasswordEncoder).apply {
            // Override the default encoder (which throws an error on use) with a simple input equals implementation.
            // This is needed because Spring OAuth Server picks up on this bean while it should not.
            setDefaultPasswordEncoderForMatches(NoopPasswordEncoder())
        }

}
