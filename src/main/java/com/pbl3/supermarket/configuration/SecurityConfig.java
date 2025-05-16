package com.pbl3.supermarket.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import java.util.List;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private CustomDecoder customDecoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())          // bật CORS theo bean bên dưới
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, 
                    "/product/**", 
                    "/category/all", 
                    "/customer/id/{customerID}",
                    "/customer/myCart/**",
                    "/customer/order",
                    "/customer/myInfo"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, 
                    "/customer", 
                    "/customer/addToCart", 
                    "/customer/removeFromCart", 
                    "/customer/order", 
                    "/auth/login", 
                    "/auth/logout", 
                    "/product/searchByCategories"
                ).permitAll()
                .requestMatchers(HttpMethod.DELETE, 
                    "/customer/{customerID}"
                ).permitAll()
                .requestMatchers(HttpMethod.PATCH,
                    "/customer/{customerID}",
                    "/customer/pass"
                ).permitAll()
                
                .requestMatchers(HttpMethod.GET, 
                    "/customer", 
                    "/supplier/all"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, 
                    "/supplier", 
                    "/category", 
                    "/product"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, 
                    "/product/{productID}"
                ).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, 
                    "/product/**"
                ).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(customDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Thay * bằng list cụ thể nếu bạn muốn đóng khung bảo mật
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter gaConverter = new JwtGrantedAuthoritiesConverter();
        gaConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(gaConverter);
        return converter;
    }
}
