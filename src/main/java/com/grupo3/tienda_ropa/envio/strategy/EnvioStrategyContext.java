package com.grupo3.tienda_ropa.envio.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EnvioStrategyContext {

    private final Map<String, EnvioStrategy> strategies;

    public EnvioStrategyContext(List<EnvioStrategy> strategyList) {
        // Mapear cada estrategia por el nombre del proveedor en mayúsculas
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getProveedor().toUpperCase(),
                        Function.identity()
                ));
    }

    /**
     * Resuelve y retorna la estrategia correspondiente para el proveedor dado.
     * Lanzará una excepción descriptiva si el proveedor no está registrado.
     */
    public EnvioStrategy getStrategy(String proveedor) {
        if (proveedor == null) {
            throw new IllegalArgumentException("El proveedor de envío no puede ser nulo");
        }
        EnvioStrategy strategy = strategies.get(proveedor.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No se encontró una estrategia de envío configurada para el proveedor: " + proveedor + 
                    ". Proveedores soportados: " + strategies.keySet()
            );
        }
        return strategy;
    }
    
    /**
     * Retorna todos los nombres de proveedores soportados.
     */
    public List<String> getProveedoresSoportados() {
        return List.copyOf(strategies.keySet());
    }
}
