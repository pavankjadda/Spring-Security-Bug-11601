package com.example.springsecuritybug11601.security;

import com.example.springsecuritybug11601.security.providers.CustomDaoAuthenticationProvider;
import com.example.springsecuritybug11601.security.userdetails.PresDbUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

import static com.example.springsecuritybug11601.constants.AuthorityConstants.*;

/**
 * Core security config class of the project
 *
 * @author Pavan Kumar Jadda
 * @since 2.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final PresDbUserDetailsService presDbUserDetailsService;

    public SecurityConfig(PresDbUserDetailsService presDbUserDetailsService) {
        this.presDbUserDetailsService = presDbUserDetailsService;
    }


    /**
     * DAO Authentication Provider that provides internal accounts for login. Only to be used in Dev and Test environments
     *
     * @author Pavan Kumar Jadda
     * @since 2.0.0
     */
    @Bean
    public CustomDaoAuthenticationProvider daoAuthenticationProvider() {
        CustomDaoAuthenticationProvider daoAuthenticationProvider = new CustomDaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(presDbUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * CORS filter to accept incoming requests
     *
     * @author Pavan Kumar Jadda
     * @since 2.0.0
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedMethods(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Bcrypt PasswordEncoder with strength 12
     *
     * @author Pavan Kumar Jadda
     * @since 2.0.0
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


    /**
     * Custom CookieCsrfTokenRepository bean to issue cookie based CSRF(XSRF) tokens
     *
     * @author Pavan Kumar Jadda
     * @since 2.6.0
     */
    @Bean
    CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        var cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setSecure(true);
        return cookieCsrfTokenRepository;
    }

    /**
     * Ignore authentication for static files requests
     *
     * @author Pavan Kumar Jadda
     * @since 2.7.16
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/static/**");
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder().username("admin").password("admin").roles("SYS_ADMIN").build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Defines SecurityFilterChain that authenticates External API users
     *
     * @author Pavan Kumar Jadda
     * @since 2.7.17
     */
    @Bean
    public SecurityFilterChain externalApiFilterChain(HttpSecurity http) throws Exception {
        http.antMatcher("/api/v1/search/**")
                .authorizeHttpRequests(registry -> registry.antMatchers("/api/v1/search/**").hasAnyAuthority(ROLE_API_USER,ROLE_SYS_ADMIN))
                .httpBasic().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.headers().frameOptions().disable();

        // Uses CorsConfigurationSource bean defined below
        http.cors().configurationSource(corsConfigurationSource());

        // Disable CSRF(XSRF) tokens for API requests
        http.csrf().disable();

        return http.build();
    }


    /**
     * Defines SecurityFilterChain that authenticates Angular client users
     *
     * @author Pavan Kumar Jadda
     * @since 2.7.17
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(registry -> registry.antMatchers( "/api/v1/user/**")
                        .hasAnyAuthority(ROLE_READ_ONLY_USER, ROLE_SYS_ADMIN))
                .authenticationProvider(daoAuthenticationProvider()).httpBasic()
                .and().logout()
                .invalidateHttpSession(true).clearAuthentication(true);

        http.headers().frameOptions().disable();

        // Uses CorsConfigurationSource bean defined below
        http.cors().configurationSource(corsConfigurationSource());

        // Use CookieCsrfTokenRepository to issue cookie based CSRF(XSRF) tokens
        http.csrf().disable();

        //Create new session only if required and Sets maximum sessions to 10 and configures Session Registry bean
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).maximumSessions(10);

        return http.build();
    }
}
