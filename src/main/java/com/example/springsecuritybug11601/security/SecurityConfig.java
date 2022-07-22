package com.example.springsecuritybug11601.security;

import com.example.springsecuritybug11601.security.providers.CustomDaoAuthenticationProvider;
import com.example.springsecuritybug11601.security.userdetails.PresDbUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;

import static com.example.springsecuritybug11601.constants.AuthorityConstants.*;

/**
 * Core security config class of the project
 *
 * @author Pavan Kumar Jadda
 * @since 1.0.0
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
     * @since 1.0.0
     */
    @Bean
    public CustomDaoAuthenticationProvider daoAuthenticationProvider() {
        CustomDaoAuthenticationProvider daoAuthenticationProvider = new CustomDaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(presDbUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    //TODO replace the LDAP Domain, URL and Authorities

    /**
     * Active Directory Authentication Provider that integrates with AD
     *
     * @author Pavan Kumar Jadda
     * @since 1.0.0
     */
    public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider = new ActiveDirectoryLdapAuthenticationProvider("domain", "url");
        activeDirectoryLdapAuthenticationProvider.setConvertSubErrorCodesToExceptions(true);
        activeDirectoryLdapAuthenticationProvider.setUserDetailsContextMapper(new UserDetailsContextMapper() {
            @Override
            public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> ldapGroups) {
                /* Add implementation */

                //Return new Spring Security user
                return new org.springframework.security.core.userdetails.User(username, "", true, true, true, true,
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
            }

            @Override
            public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
                //Implementation not needed
            }
        });

        return activeDirectoryLdapAuthenticationProvider;
    }

    /**
     * CORS filter to accept incoming requests
     *
     * @author Pavan Kumar Jadda
     * @since 1.0.0
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
     * @since 1.0.0
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

    /**
     * Defines SecurityFilterChain that authenticates External API users
     *
     * @author Pavan Kumar Jadda
     * @since 2.7.17
     */
    @Bean
    public SecurityFilterChain externalApiFilterChain(HttpSecurity http) throws Exception {
        http.antMatcher("/api/v1/search/**")
                .authorizeHttpRequests(registry -> registry.antMatchers("/api/v1/search/**").hasAnyAuthority(ROLE_API_USER, ROLE_SYS_ADMIN))
                .authenticationProvider(activeDirectoryLdapAuthenticationProvider()).httpBasic().and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

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
        http.authorizeHttpRequests(registry -> registry.antMatchers("/api/v1/user/**").hasAnyAuthority(ROLE_READ_ONLY_USER, ROLE_SYS_ADMIN))
                .authenticationProvider(daoAuthenticationProvider()).authenticationProvider(activeDirectoryLdapAuthenticationProvider()).httpBasic().and()
                .logout().invalidateHttpSession(true).clearAuthentication(true);

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
