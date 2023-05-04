package nl.juraji.charactersheetscentral.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.security.SecureRandom


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
                    .defaultSuccessUrl("/members")
            }
            .logout { it.permitAll() }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(
        clients: RegisteredClientRepository
    ): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.addAllowedOrigin("http://localhost:8080")


        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/oauth2/**", config)
        return source
    }

    @Bean
    fun sessionRegistry(): SessionRegistry = SessionRegistryImpl()

    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher = HttpSessionEventPublisher()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(-1, SecureRandom())

}
