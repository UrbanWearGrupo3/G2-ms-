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
import org.springframework.security.config.Customizer;
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
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas de autenticación, base de datos h2, redirecciones de Mercado Pago y errores
                        .requestMatchers("/api/auth/**", "/h2-console/**", "/api/internal/**", "/api/pedidos/pago/**", "/error")
                        .permitAll()
                        // El catálogo de ropa es público para lectura
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                        // Carga de imágenes de productos (solo administradores)
                        .requestMatchers(HttpMethod.POST, "/api/productos/upload").hasAnyRole("ADMIN", "SUPER_USER")
                        // Solo administradores pueden agregar/modificar productos y variantes
                        .requestMatchers("/api/productos/**").hasAnyRole("ADMIN", "SUPER_USER")
                        // Los usuarios autenticados pueden ver y actualizar su perfil
                        .requestMatchers("/api/usuarios/me").authenticated()
                        // Solo administradores pueden listar o gestionar otros usuarios
                        .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "SUPER_USER")
                        // Catálogo de colores es público para lectura
                        .requestMatchers(HttpMethod.GET, "/api/colores", "/api/colores/**").permitAll()
                        // Solo administradores pueden agregar/modificar colores
                        .requestMatchers("/api/colores/**").hasAnyRole("ADMIN", "SUPER_USER")
                        // Catálogo de categorías es público para lectura
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
                        // Solo administradores pueden agregar/modificar categorías
                        .requestMatchers("/api/categorias/**").hasAnyRole("ADMIN", "SUPER_USER")
                        // Cupones: Validar es para cualquier usuario autenticado, el resto es para ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/cupones/validar").authenticated()
                        .requestMatchers("/api/cupones/**").hasAnyRole("ADMIN", "SUPER_USER")
                        // Pedidos: Solo administradores pueden ver todos los pedidos o actualizar estados generales
                        .requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/estado/**").hasAnyRole("ADMIN", "SUPER_USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado").hasAnyRole("ADMIN", "SUPER_USER")
                        // Rutas exclusivas para Superusuario
                        .requestMatchers("/api/super-user/**").hasRole("SUPER_USER")
                        // Envios: Modificar estado de envíos es solo para administradores
                        .requestMatchers(HttpMethod.PUT, "/api/envios/**").hasAnyRole("ADMIN", "SUPER_USER")
                        .requestMatchers("/api/envios/**").authenticated()
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
