package com.grupo3.tienda_ropa.config;

import com.grupo3.tienda_ropa.color.entity.Color;
import com.grupo3.tienda_ropa.color.repository.ColorRepository;

import com.grupo3.tienda_ropa.producto.entity.Categoria;
import com.grupo3.tienda_ropa.producto.repository.CategoriaRepository;
import com.grupo3.tienda_ropa.usuario.entity.Rol;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ColorRepository colorRepository;
    public DataSeeder(CategoriaRepository categoriaRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      ColorRepository colorRepository) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.colorRepository = colorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        seedCategorias();
        seedUsuarios();
        seedColores();
    }

    private void seedCategorias() {
        if (categoriaRepository.count() == 0) {
            Categoria remeras = new Categoria();
            remeras.setNombre("Remeras");
            remeras.setDescripcion("Remeras, musculosas y chombas de algodón y poliéster");
            remeras.setActivo(true);

            Categoria pantalones = new Categoria();
            pantalones.setNombre("Pantalones");
            pantalones.setDescripcion("Jeans, joggers, pantalones formales y bermudas");
            pantalones.setActivo(true);

            Categoria camperas = new Categoria();
            camperas.setNombre("Camperas");
            camperas.setDescripcion("Camperas, buzos, sacos y abrigos para invierno");
            camperas.setActivo(true);

            Categoria calzado = new Categoria();
            calzado.setNombre("Calzado");
            calzado.setDescripcion("Zapatillas deportivas, urbanas, botas y sandalias");
            calzado.setActivo(true);

            Categoria accesorios = new Categoria();
            accesorios.setNombre("Accesorios");
            accesorios.setDescripcion("Gorras, cinturones, mochilas y anteojos");
            accesorios.setActivo(true);

            categoriaRepository.saveAll(List.of(remeras, pantalones, camperas, calzado, accesorios));
        }
    }

    private void seedUsuarios() {
        if (usuarioRepository.count() == 0) {
            // Admin por defecto para pruebas
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setApellido("UrbanWear");
            admin.setEmail("admin@urbanwear.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);

            // Cliente por defecto para pruebas
            Usuario cliente = new Usuario();
            cliente.setNombre("Cliente");
            cliente.setApellido("Demo");
            cliente.setEmail("cliente@demo.com");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            cliente.setRol(Rol.CLIENTE);
            cliente.setActivo(true);

            usuarioRepository.saveAll(List.of(admin, cliente));
        }
    }

    private void seedColores() {
        if (colorRepository.count() == 0) {
            Color negro = new Color();
            negro.setNombre("Negro");
            negro.setCodigoHex("#000000");

            Color blanco = new Color();
            blanco.setNombre("Blanco");
            blanco.setCodigoHex("#FFFFFF");

            Color rojo = new Color();
            rojo.setNombre("Rojo");
            rojo.setCodigoHex("#FF0000");

            Color azul = new Color();
            azul.setNombre("Azul");
            azul.setCodigoHex("#0000FF");

            Color verde = new Color();
            verde.setNombre("Verde");
            verde.setCodigoHex("#008000");

            Color gris = new Color();
            gris.setNombre("Gris");
            gris.setCodigoHex("#808080");

            Color beige = new Color();
            beige.setNombre("Beige");
            beige.setCodigoHex("#F5F5DC");

            Color bordo = new Color();
            bordo.setNombre("Bordó");
            bordo.setCodigoHex("#800020");

            Color celeste = new Color();
            celeste.setNombre("Celeste");
            celeste.setCodigoHex("#87CEEB");

            Color rosa = new Color();
            rosa.setNombre("Rosa");
            rosa.setCodigoHex("#FFC0CB");

            colorRepository.saveAll(List.of(negro, blanco, rojo, azul, verde, gris, beige, bordo, celeste, rosa));
        }
    }


}
