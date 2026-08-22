package com.picturebook.global.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.picturebook.global.security.oauth2.OAuth2SuccessHandler;
import com.picturebook.global.security.oauth2.CustomOAuth2UserService;
import com.picturebook.global.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        @Value("${app.frontend-url}")
        private String frontendUrl;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(conf -> conf
                //302 대신 401을 던지게 함
                        .authenticationEntryPoint((request, response, authException) -> {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        })
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/logout",
                                "/api/user/me",
                                "/api/user/me/**",
                                "/api/books/me",
                                "/api/books/me/**",
                                "/api/storage/**",
                                "/api/report/**",
                                "/api/reviews/me"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/books/*/reviews"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/reviews/*"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/reviews/*"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/books/*/likes"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/books/*/publish/paid"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/books/*/likes"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/authors/*/follow"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/authors/*/follow"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/reading-goals"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/reading-goals"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/user/*/profile"
                        ).permitAll()
                        .requestMatchers(
                                "/login/**",
                                "/oauth2/**",
                                "/api/auth/refresh"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(userInfo->userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
        }
        @Bean
        public CorsConfigurationSource corsConfigurationSource(){
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(frontendUrl));
                configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setExposedHeaders(Arrays.asList("X-Request-Id"));
                configuration.setAllowCredentials(true); // 쿠키를 주고받으려면 true로 설정 -rt 쿠키

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
