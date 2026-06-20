package com.grupo3.tienda_ropa;

// SDK de Mercado Pago
import com.mercadopago.MercadoPagoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TiendaRopaApplication {

	public static void main(String[] args) {
		try {
			java.io.File envFile = new java.io.File(".env");
			if (envFile.exists()) {
				java.nio.file.Files.lines(envFile.toPath())
						.map(String::trim)
						.filter(line -> !line.isEmpty() && !line.startsWith("#"))
						.forEach(line -> {
							String[] parts = line.split("=", 2);
							if (parts.length == 2) {
								System.setProperty(parts[0].trim(), parts[1].trim());
							}
						});
			}
		} catch (Exception e) {
			// Ignorar errores al cargar .env en entornos donde se inyectan variables nativas
		}

		// Agrega credenciales
		String accessToken = System.getProperty("MERCADOPAGO_ACCESS_TOKEN", System.getenv("MERCADOPAGO_ACCESS_TOKEN"));
		if (accessToken != null && !accessToken.trim().isEmpty()) {
			MercadoPagoConfig.setAccessToken(accessToken);
		} else {
			System.err.println("Warning: MERCADOPAGO_ACCESS_TOKEN not found in environment properties or .env file.");
		}

		SpringApplication.run(TiendaRopaApplication.class, args);
	}

}
