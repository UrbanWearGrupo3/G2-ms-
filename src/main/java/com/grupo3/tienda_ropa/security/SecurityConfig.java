package com.grupo3.tienda_ropa.security;

import com.grupo3.tienda_ropa.usuario.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UsuarioRepository usuarioRepository, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.usuarioRepository = usuarioRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas de autenticación, base de datos h2 y redirecciones de Mercado
                        // Pago
                        .requestMatchers("/api/auth/**", "/h2-console/**", "/api/internal/**", "/api/pedidos/pago/**")
                        .permitAll()
                        // El catálogo de ropa es público para lectura
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                        // Las reseñas son públicas para lectura, pero crear/editar/eliminar requiere
                        // autenticación
                        .requestMatchers(HttpMethod.GET, "/api/resenias/**").permitAll()
                        .requestMatchers("/api/resenias/**").authenticated()
                        // Carga de imágenes de productos (solo administradores)
                        .requestMatchers(HttpMethod.POST, "/api/productos/upload").hasRole("ADMIN")
                        // SOLO LOS ADMINS PUEDEN INGRESAR A TODOS LOS PEDIDOS
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasRole("ADMIN")
                        // Solo administradores pueden agregar/modificar productos y variantes
                        .requestMatchers("/api/productos/**").hasRole("ADMIN")
                        // Los usuarios autenticados pueden ver y actualizar su perfil
                        .requestMatchers("/api/usuarios/me").authenticated()
                        // Solo administradores pueden listar o gestionar otros usuarios
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Cualquier otra petición requiere login básico
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
