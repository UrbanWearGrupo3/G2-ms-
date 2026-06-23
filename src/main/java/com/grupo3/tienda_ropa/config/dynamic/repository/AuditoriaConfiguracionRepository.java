package com.grupo3.tienda_ropa.config.dynamic.repository;

import com.grupo3.tienda_ropa.config.dynamic.entity.AuditoriaConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditoriaConfiguracionRepository extends JpaRepository<AuditoriaConfiguracion, Long> {
    List<AuditoriaConfiguracion> findAllByOrderByFechaCambioDesc();
}
