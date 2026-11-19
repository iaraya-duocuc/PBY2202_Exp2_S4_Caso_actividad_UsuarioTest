package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.repository.ProductoRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {
    @Mock
    private UsuarioService usuarios;

    @Mock
    private ProductoRepository productos;

    @Mock
    private VentaRepository ventas;

    @InjectMocks
    private VentaServiceImpl servicio;

    @ParameterizedTest
    @ValueSource(strings = {"Usuario no encontrado", "El email del usuario es obligatorio",
            "El rol del usuario no está autorizado para realizar ventas"})
    void rechazaVentaSiUsuarioNoHabilitaLaOperacion(String mensaje) {
        when(usuarios.obtenerUsuarioHabilitadoParaVenta(1L))
                .thenThrow(new IllegalArgumentException(mensaje));

        assertEquals(mensaje, assertThrows(IllegalArgumentException.class,
                () -> servicio.realizarVenta(1L, List.of(10L))).getMessage());
        verifyNoInteractions(productos, ventas);
    }

    @Test
    void calculaTotalVinculaUsuarioYProductosYDescuentaStock() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Producto alimento = producto(10L, "Alimento", "12.30", 2);
        Producto suplemento = producto(20L, "Suplemento", "7.45", 1);
        List<Long> ids = List.of(10L, 20L);
        when(usuarios.obtenerUsuarioHabilitadoParaVenta(1L)).thenReturn(usuario);
        when(productos.findAllById(ids)).thenReturn(List.of(alimento, suplemento));
        when(ventas.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Venta venta = servicio.realizarVenta(1L, ids);

        assertSame(usuario, venta.getUsuario());
        assertEquals(List.of(alimento, suplemento), venta.getProductos());
        assertEquals(new BigDecimal("19.75"), venta.getTotal());
        assertNotNull(venta.getFecha());
        assertEquals(1, alimento.getStock());
        assertEquals(0, suplemento.getStock());
        verify(productos).save(alimento);
        verify(productos).save(suplemento);
        verify(ventas).save(same(venta));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rechazaStockInsuficienteSinModificarOtrosProductos(Integer stock) {
        Producto alimento = producto(10L, "Alimento", "12.30", 2);
        Producto suplemento = producto(20L, "Suplemento", "7.45", stock);
        List<Long> ids = List.of(10L, 20L);
        when(productos.findAllById(ids)).thenReturn(List.of(alimento, suplemento));

        assertEquals("El producto Suplemento no tiene stock disponible",
                assertThrows(IllegalArgumentException.class,
                        () -> servicio.realizarVenta(1L, ids)).getMessage());
        assertEquals(2, alimento.getStock());
        assertEquals(stock, suplemento.getStock());
        verify(productos, never()).save(any());
        verifyNoInteractions(ventas);
    }

    @Test
    void rechazaVentaConProductosInexistentes() {
        List<Long> ids = List.of(10L, 20L);
        when(productos.findAllById(ids)).thenReturn(List.of(producto(10L, "Alimento", "12.30", 2)));

        assertEquals("Algunos productos no fueron encontrados", assertThrows(IllegalArgumentException.class,
                () -> servicio.realizarVenta(1L, ids)).getMessage());
        verify(productos, never()).save(any());
        verifyNoInteractions(ventas);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rechazaVentaSinProductos(List<Long> ids) {
        assertEquals("La venta debe incluir al menos un producto", assertThrows(IllegalArgumentException.class,
                () -> servicio.realizarVenta(1L, ids)).getMessage());
        verifyNoInteractions(productos, ventas);
    }

    @Test
    void consultaVentaPorId() {
        Venta venta = Venta.builder().id(2L).build();
        when(ventas.findById(2L)).thenReturn(Optional.of(venta));

        assertSame(venta, servicio.obtenerVentaPorId(2L).orElseThrow());
    }

    @Test
    void listaVentasPorUsuario() {
        List<Venta> registros = List.of(Venta.builder().id(2L).build());
        when(ventas.findByUsuarioId(1L)).thenReturn(registros);

        assertEquals(registros, servicio.listarVentasPorUsuario(1L));
    }

    @Test
    void listaTodasLasVentas() {
        List<Venta> registros = List.of(Venta.builder().id(2L).build());
        when(ventas.findAll()).thenReturn(registros);

        assertEquals(registros, servicio.listarTodasLasVentas());
    }

    private Producto producto(Long id, String nombre, String precio, Integer stock) {
        return Producto.builder().id(id).nombre(nombre).precio(new BigDecimal(precio)).stock(stock).build();
    }
}
