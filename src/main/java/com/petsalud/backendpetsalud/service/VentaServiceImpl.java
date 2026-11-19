package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.repository.ProductoRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional
    public Venta realizarVenta(Long usuarioId, List<Long> productosIds) {
        Usuario usuario = usuarioService.obtenerUsuarioHabilitadoParaVenta(usuarioId);

        if (productosIds == null || productosIds.isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto");
        }

        List<Producto> productos = productoRepository.findAllById(productosIds);

        if (productos.size() != productosIds.size()) {
            throw new IllegalArgumentException("Algunos productos no fueron encontrados");
        }

        for (Producto producto : productos) {
            if (producto.getStock() == null || producto.getStock() <= 0) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no tiene stock disponible");
            }
        }

        BigDecimal total = productos.stream()
                .map(Producto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (Producto producto : productos) {
            producto.setStock(producto.getStock() - 1);
            productoRepository.save(producto);
        }

        Venta venta = Venta.builder()
                .usuario(usuario)
                .productos(productos)
                .total(total)
                .fecha(LocalDateTime.now())
                .build();

        return ventaRepository.save(venta);
    }

    @Override
    public Optional<Venta> obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }

    @Override
    public List<Venta> listarVentasPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<Venta> listarTodasLasVentas() {
        return ventaRepository.findAll();
    }
}

