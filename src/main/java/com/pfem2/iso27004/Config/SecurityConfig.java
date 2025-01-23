package com.pfem2.iso27004.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.pfem2.iso27004.Security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthoFilter;

    @Autowired
    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtAuthenticationFilter jwtAuthoFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthoFilter = jwtAuthoFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors();
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/generate-pdf").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/indicator/**").hasAnyAuthority("ADMIN", "USER",
                            "COLLECTOR");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/indicator/**").hasAnyAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/indicator/**").hasAnyAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v1/indicator/**").hasAnyAuthority("ADMIN");

                    auth.requestMatchers(HttpMethod.GET, "/api/v1/evaluation/**").hasAnyAuthority("ADMIN", "USER",
                            "COLLECTOR");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/evaluation/**").hasAuthority("COLLECTOR");

                    auth.requestMatchers("/api/v1/user/collector/**").permitAll();
                    auth.requestMatchers("/api/v1/user/reset/**").hasAuthority("ADMIN");

                    auth.requestMatchers("/api/v1/user/**").hasAuthority("ADMIN");
                    auth.requestMatchers("/api/v1/app/**").hasAuthority("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/auth/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthoFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        // configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
