package pe.edu.upeu.lp2_gp2.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pe.edu.upeu.lp2_gp2.exceptions.CustomAccessDeniedHandler;
import pe.edu.upeu.lp2_gp2.exceptions.CustomAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {
    @Bean
    HttpSessionSecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CookieCsrfTokenRepository csrfCookieRepository(@Value("${server.servlet.session.cookie.secure}") boolean secure) {
        var repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.path("/").sameSite("Lax").secure(secure));
        return repository;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                           HttpSessionSecurityContextRepository sessions,
                                           CookieCsrfTokenRepository csrfCookies,
                                           CustomAccessDeniedHandler accessDenied,
                                           CustomAuthenticationEntryPoint unauthenticated,
                                           @Value("${server.servlet.session.cookie.secure}") boolean secure) throws Exception {
        String webhook = "/api/v1/pagos/mercadopago/webhook";
        Cookie sessionCookie = new Cookie("LP2SESSION", "");
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(secure);

        return http
                .securityContext(context -> context.securityContextRepository(sessions))
                .requestCache(cache -> cache.disable())
                .csrf(csrf -> csrf.csrfTokenRepository(csrfCookies)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(new AntPathRequestMatcher(webhook, "POST")))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf", "/api/v1/auth/session").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/registro", webhook).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/images/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/api/v1/productos/**", "/images/**").permitAll()
                        .requestMatchers("/api/v1/productos/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/pedidos/**").hasAuthority("CLIENTE")
                        .anyRequest().denyAll())
                .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(new CookieClearingLogoutHandler(sessionCookie))
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(
                                org.springframework.http.HttpStatus.NO_CONTENT)))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthenticated).accessDeniedHandler(accessDenied))
                .build();
    }
}
