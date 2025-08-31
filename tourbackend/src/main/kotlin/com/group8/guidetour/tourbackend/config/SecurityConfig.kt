package com.group8.guidetour.tourbackend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/users/register",
                    "/api/users/login",
                    "/api/cities",
                    "/api/cities/*",
                    "/api/cities/**/places"
                ).permitAll()
                it.anyRequest().authenticated()
            }
            .formLogin { it.disable() }
        return http.build()
    }
}
