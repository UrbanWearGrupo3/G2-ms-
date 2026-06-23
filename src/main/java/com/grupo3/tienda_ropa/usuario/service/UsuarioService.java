package com.grupo3.tienda_ropa.usuario.service;

import com.grupo3.tienda_ropa.security.JwtService;
import com.grupo3.tienda_ropa.usuario.deto.*;
import com.grupo3.tienda_ropa.usuario.entity.Rol;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${security.admin-passcode:}")
    private String adminPasscode;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          JwtService jwtService, AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UsuarioResponse register(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setActivo(true);

        // Si se provee el passcode de admin y coincide, se registra como ADMIN
        if (request.getAdminPasscode() != null && request.getAdminPasscode().equals(adminPasscode)) {
            usuario.setRol(Rol.ADMIN);
        } else {
            usuario.setRol(Rol.CLIENTE);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return mapToResponse(usuarioGuardado);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + request.getEmail()));

        if (!usuario.getActivo()) {
            throw new IllegalStateException("Esta cuenta se encuentra desactivada");
        }

        String jwtToken = jwtService.generateToken(usuario);

        LoginResponse response = new LoginResponse();
        response.setToken(jwtToken);
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol().name());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());

        return response;
    }
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
    public UsuarioResponse getProfile(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return mapToResponse(usuario);
    }

    @Transactional
    public UsuarioResponse updateProfile(String email, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapToResponse(usuarioActualizado);
    }

    public Page<UsuarioResponse> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::mapToResponse);
    }
  
    @Transactional
    public UsuarioResponse updateRol(Long id, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Obtener el rol del usuario autenticado
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isSuperUser = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_USER"));

        if (!isSuperUser) {
            // Si el usuario objetivo es SUPER_USER, un ADMIN no puede modificarlo
            if (usuario.getRol() == Rol.SUPER_USER) {
                throw new org.springframework.security.access.AccessDeniedException("No tienes permisos para modificar a un superusuario");
            }
            // Si se intenta asignar el rol SUPER_USER, un ADMIN no puede hacerlo
            if (nuevoRol == Rol.SUPER_USER) {
                throw new org.springframework.security.access.AccessDeniedException("Un administrador no puede asignar el rol de superusuario");
            }
        }

        usuario.setRol(nuevoRol);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapToResponse(usuarioActualizado);
    }

    @Transactional
    public void delete(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Prevenir que un administrador desactive a un superusuario
        if (usuario.getRol() == Rol.SUPER_USER) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperUser = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_USER"));
            if (!isSuperUser) {
                throw new org.springframework.security.access.AccessDeniedException("No tienes permisos para desactivar a un superusuario");
            }
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        UsuarioResponse resp = new UsuarioResponse();
        resp.setId(usuario.getId());
        resp.setNombre(usuario.getNombre());
        resp.setApellido(usuario.getApellido());
        resp.setEmail(usuario.getEmail());
        resp.setRol(usuario.getRol().name());
        resp.setActivo(usuario.getActivo());
        resp.setFechaCreacion(usuario.getFechaCreacion());
        return resp;
    }
    public Usuario obtenerUsuarioPorEmail(String email) {

    return usuarioRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Usuario no encontrado"
                    ));
}
}
