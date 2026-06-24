package com.grupo3.tienda_ropa;

// SDK de Mercado Pago
import com.mercadopago.MercadoPagoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class TiendaRopaApplication {

	public static void main(String[] args) {
		try {
			java.io.File envFile = new java.io.File(".env");
			if (envFile.exists()) {
				java.nio.file.Files.lines(envFile.toPath())
						.map(String::trim)
						.filter(line -> !line.isEmpty() && !line.startsWith("#"))
						.forEach(line -> {
							int eqIdx = line.indexOf('=');
							if (eqIdx > 0) {
								String key = line.substring(0, eqIdx).trim();
								String value = line.substring(eqIdx + 1).trim();
								// Eliminar comillas simples o dobles que rodean el valor
								if ((value.startsWith("\"") && value.endsWith("\"")) ||
									(value.startsWith("'") && value.endsWith("'"))) {
									value = value.substring(1, value.length() - 1);
								}
								System.setProperty(key, value);
							}
						});
				System.out.println("✅ Archivo .env cargado correctamente");
			} else {
				System.out.println("INFO: No se encontró archivo .env, usando variables de entorno del sistema.");
			}
		} catch (Exception e) {
			System.err.println("ERROR al cargar .env: " + e.getMessage());
		}

		// Configurar Mercado Pago lo antes posible
		String accessToken = System.getProperty("MERCADOPAGO_ACCESS_TOKEN",
				System.getenv("MERCADOPAGO_ACCESS_TOKEN"));
		if (accessToken != null && !accessToken.trim().isEmpty()) {
			MercadoPagoConfig.setAccessToken(accessToken.trim());
			String preview = accessToken.length() > 8
					? accessToken.substring(0, 6) + "..." + accessToken.substring(accessToken.length() - 4)
					: "(muy corto)";
			System.out.println("✅ MercadoPago configurado al inicio. Token: " + preview + " (longitud: " + accessToken.length() + ")");
		} else {
			System.out.println("WARN: MERCADOPAGO_ACCESS_TOKEN no encontrado. El pago no funcionará.");
		}

		SpringApplication.run(TiendaRopaApplication.class, args);
	}


	@org.springframework.context.annotation.Bean
	public org.springframework.cache.CacheManager cacheManager() {
		return new org.springframework.cache.concurrent.ConcurrentMapCacheManager("configuraciones");
	}
}
