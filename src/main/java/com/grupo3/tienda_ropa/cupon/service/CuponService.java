package com.grupo3.tienda_ropa.cupon.service;

import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.repository.CarritoItemRepo;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.cupon.dto.CuponDescuentoDto;
import com.grupo3.tienda_ropa.cupon.dto.CuponRequestDto;
import com.grupo3.tienda_ropa.cupon.dto.CuponResponseDto;
import com.grupo3.tienda_ropa.cupon.entity.Cupon;
import com.grupo3.tienda_ropa.cupon.entity.TipoDescuento;
import com.grupo3.tienda_ropa.cupon.repository.CuponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuponService {

    private final CuponRepository cuponRepository;
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepo carritoItemRepo;
    private final PedidosRepository pedidosRepository;

    @Transactional
    public CuponResponseDto crearCupon(CuponRequestDto dto) {
        if (cuponRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new RuntimeException("Ya existe un cupón con el código: " + dto.getCodigo());
        }

        Cupon cupon = new Cupon();
        updateCuponFields(cupon, dto);

        Cupon saved = cuponRepository.save(cupon);
        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CuponResponseDto> obtenerTodosLosCupones() {
        return cuponRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CuponResponseDto obtenerCuponPorId(Long id) {
        Cupon cupon = cuponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cupón no encontrado"));
        return mapToResponseDto(cupon);
    }

    @Transactional
    public CuponResponseDto actualizarCupon(Long id, CuponRequestDto dto) {
        Cupon cupon = cuponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cupón no encontrado"));

        if (!cupon.getCodigo().equalsIgnoreCase(dto.getCodigo()) &&
                cuponRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new RuntimeException("Ya existe otro cupón con el código: " + dto.getCodigo());
        }

        updateCuponFields(cupon, dto);
        Cupon updated = cuponRepository.save(cupon);
        return mapToResponseDto(updated);
    }

    @Transactional
    public void eliminarCupon(Long id) {
        if (!cuponRepository.existsById(id)) {
            throw new RuntimeException("Cupón no encontrado");
        }
        cuponRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CuponDescuentoDto validarYCalcularDescuento(String codigo, Long usuarioId) {
        Optional<Cupon> cuponOpt = cuponRepository.findByCodigoIgnoreCase(codigo);
        if (cuponOpt.isEmpty()) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(BigDecimal.ZERO)
                    .mensajeError("El cupón no existe.")
                    .build();
        }

        Cupon cupon = cuponOpt.get();

        if (!cupon.getActivo()) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(BigDecimal.ZERO)
                    .mensajeError("El cupón no está activo.")
                    .build();
        }

        if (LocalDateTime.now().isAfter(cupon.getFechaExpiracion())) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(BigDecimal.ZERO)
                    .mensajeError("El cupón ha expirado.")
                    .build();
        }

        if (cupon.getLimiteUso() != null && cupon.getVecesUsado() >= cupon.getLimiteUso()) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(BigDecimal.ZERO)
                    .mensajeError("El cupón ha alcanzado su límite de uso global.")
                    .build();
        }

        // Validar si el cliente ya usó este cupón (excluyendo estados de pago rechazados/fallidos)
        if (!cupon.getPermiteMultiplesUsosPorCliente()) {
            boolean yaUsado = pedidosRepository.existsByUsuarioIdAndCuponCodigoIgnoreCaseAndEstadoNotIn(
                    usuarioId, codigo, List.of("RECHAZADO", "FALLIDO")
            );
            if (yaUsado) {
                return CuponDescuentoDto.builder()
                        .codigo(codigo)
                        .valido(false)
                        .descuentoAplicado(BigDecimal.ZERO)
                        .nuevoTotal(BigDecimal.ZERO)
                        .mensajeError("Ya has utilizado este cupón en una compra anterior.")
                        .build();
            }
        }

        CarritoEntity carrito = carritoRepository.findByUsuario_Id(usuarioId)
                .orElse(null);
        if (carrito == null) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(BigDecimal.ZERO)
                    .mensajeError("Carrito no encontrado.")
                    .build();
        }

        List<CarritoItem> items = carritoItemRepo.findByCarritoId(carrito.getId());
        if (items.isEmpty()) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(BigDecimal.ZERO)
                    .mensajeError("El carrito está vacío.")
                    .build();
        }

        BigDecimal subtotal = items.stream()
                .map(item -> item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return validarYCalcularDescuento(codigo, usuarioId, subtotal);
    }

    @Transactional(readOnly = true)
    public CuponDescuentoDto validarYCalcularDescuento(String codigo, Long usuarioId, BigDecimal subtotal) {
        Optional<Cupon> cuponOpt = cuponRepository.findByCodigoIgnoreCase(codigo);
        if (cuponOpt.isEmpty()) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(subtotal)
                    .mensajeError("El cupón no existe.")
                    .build();
        }

        Cupon cupon = cuponOpt.get();

        if (!cupon.getActivo()) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(subtotal)
                    .mensajeError("El cupón no está activo.")
                    .build();
        }

        if (LocalDateTime.now().isAfter(cupon.getFechaExpiracion())) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(subtotal)
                    .mensajeError("El cupón ha expirado.")
                    .build();
        }

        if (cupon.getLimiteUso() != null && cupon.getVecesUsado() >= cupon.getLimiteUso()) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(subtotal)
                    .mensajeError("El cupón ha alcanzado su límite de uso global.")
                    .build();
        }

        // Validar si el cliente ya usó este cupón (excluyendo estados de pago rechazados/fallidos)
        if (!cupon.getPermiteMultiplesUsosPorCliente()) {
            boolean yaUsado = pedidosRepository.existsByUsuarioIdAndCuponCodigoIgnoreCaseAndEstadoNotIn(
                    usuarioId, codigo, List.of("RECHAZADO", "FALLIDO")
            );
            if (yaUsado) {
                return CuponDescuentoDto.builder()
                        .codigo(codigo)
                        .valido(false)
                        .descuentoAplicado(BigDecimal.ZERO)
                        .nuevoTotal(subtotal)
                        .mensajeError("Ya has utilizado este cupón en una compra anterior.")
                        .build();
            }
        }

        if (cupon.getMontoMinimo() != null && subtotal.compareTo(cupon.getMontoMinimo()) < 0) {
            return CuponDescuentoDto.builder()
                    .codigo(codigo)
                    .valido(false)
                    .descuentoAplicado(BigDecimal.ZERO)
                    .nuevoTotal(subtotal)
                    .mensajeError("El monto de la compra no alcanza el mínimo requerido para este cupón ($" + cupon.getMontoMinimo() + ").")
                    .build();
        }

        // Calcular descuento
        BigDecimal descuento = BigDecimal.ZERO;
        if (cupon.getTipoDescuento() == TipoDescuento.PORCENTAJE) {
            descuento = subtotal.multiply(cupon.getValor())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (cupon.getTipoDescuento() == TipoDescuento.FIJO) {
            descuento = cupon.getValor().min(subtotal);
        }

        BigDecimal nuevoTotal = subtotal.subtract(descuento);

        return CuponDescuentoDto.builder()
                .codigo(cupon.getCodigo())
                .valido(true)
                .descuentoAplicado(descuento)
                .nuevoTotal(nuevoTotal)
                .build();
    }

    @Transactional
    public void registrarUso(String codigo) {
        cuponRepository.findByCodigoIgnoreCase(codigo).ifPresent(cupon -> {
            cupon.setVecesUsado(cupon.getVecesUsado() + 1);
            cuponRepository.save(cupon);
        });
    }

    private void updateCuponFields(Cupon cupon, CuponRequestDto dto) {
        cupon.setCodigo(dto.getCodigo().toUpperCase().trim());
        cupon.setTipoDescuento(dto.getTipoDescuento());
        cupon.setValor(dto.getValor());
        cupon.setFechaExpiracion(dto.getFechaExpiracion());
        cupon.setMontoMinimo(dto.getMontoMinimo());
        if (dto.getActivo() != null) {
            cupon.setActivo(dto.getActivo());
        }
        cupon.setLimiteUso(dto.getLimiteUso());
        if (dto.getPermiteMultiplesUsosPorCliente() != null) {
            cupon.setPermiteMultiplesUsosPorCliente(dto.getPermiteMultiplesUsosPorCliente());
        }
    }

    private CuponResponseDto mapToResponseDto(Cupon cupon) {
        return CuponResponseDto.builder()
                .id(cupon.getId())
                .codigo(cupon.getCodigo())
                .tipoDescuento(cupon.getTipoDescuento())
                .valor(cupon.getValor())
                .fechaExpiracion(cupon.getFechaExpiracion())
                .montoMinimo(cupon.getMontoMinimo())
                .activo(cupon.getActivo())
                .limiteUso(cupon.getLimiteUso())
                .vecesUsado(cupon.getVecesUsado())
                .permiteMultiplesUsosPorCliente(cupon.getPermiteMultiplesUsosPorCliente())
                .build();
    }
}
