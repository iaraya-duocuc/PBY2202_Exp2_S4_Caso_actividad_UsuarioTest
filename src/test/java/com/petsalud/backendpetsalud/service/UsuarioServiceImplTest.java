package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {
    @Mock
    private UsuarioRepository repositorio;

    @InjectMocks
    private UsuarioServiceImpl servicio;

    @ParameterizedTest
    @EnumSource(value = Rol.class, names = {"ADMIN", "CLIENTE"})
    void admiteUsuariosCompletosConRolHabilitado(Rol rol) {
        Usuario usuario = usuarioValido();
        usuario.setRol(rol);
        when(repositorio.findById(1L)).thenReturn(Optional.of(usuario));

        assertSame(usuario, servicio.obtenerUsuarioHabilitadoParaVenta(1L));
        verify(repositorio).findById(1L);
    }

    @ParameterizedTest
    @MethodSource("datosObligatorios")
    void rechazaDatosAusentesOVacios(BiConsumer<Usuario, String> asignar, String mensaje) {
        for (String valor : new String[]{null, "", "  "}) {
            Usuario usuario = usuarioValido();
            asignar.accept(usuario, valor);
            when(repositorio.findById(1L)).thenReturn(Optional.of(usuario));

            assertEquals(mensaje, assertThrows(IllegalArgumentException.class,
                    () -> servicio.obtenerUsuarioHabilitadoParaVenta(1L)).getMessage());
        }
        verify(repositorio, never()).save(any());
    }

    static Stream<Arguments> datosObligatorios() {
        return Stream.of(
                Arguments.of((BiConsumer<Usuario, String>) Usuario::setNombre,
                        "El nombre del usuario es obligatorio"),
                Arguments.of((BiConsumer<Usuario, String>) Usuario::setApellido,
                        "El apellido del usuario es obligatorio"),
                Arguments.of((BiConsumer<Usuario, String>) Usuario::setEmail,
                        "El email del usuario es obligatorio"),
                Arguments.of((BiConsumer<Usuario, String>) Usuario::setDireccion,
                        "La dirección del usuario es obligatoria"));
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = Rol.class, names = "VETERINARIO")
    void rechazaRolesNoAutorizados(Rol rol) {
        Usuario usuario = usuarioValido();
        usuario.setRol(rol);
        when(repositorio.findById(1L)).thenReturn(Optional.of(usuario));

        assertEquals("El rol del usuario no está autorizado para realizar ventas",
                assertThrows(IllegalArgumentException.class,
                        () -> servicio.obtenerUsuarioHabilitadoParaVenta(1L)).getMessage());
    }

    @Test
    void rechazaUsuarioInexistenteParaVenta() {
        when(repositorio.findById(1L)).thenReturn(Optional.empty());

        assertEquals("Usuario no encontrado", assertThrows(IllegalArgumentException.class,
                () -> servicio.obtenerUsuarioHabilitadoParaVenta(1L)).getMessage());
    }

    @Test
    void registraUsuarioConEmailDisponible() {
        Usuario usuario = usuarioValido();
        when(repositorio.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        when(repositorio.save(usuario)).thenReturn(usuario);

        assertSame(usuario, servicio.registrarUsuario(usuario));
    }

    @Test
    void rechazaRegistroConEmailDuplicado() {
        Usuario usuario = usuarioValido();
        when(repositorio.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertEquals("El email ya está registrado", assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarUsuario(usuario)).getMessage());
        verify(repositorio, never()).save(any());
    }

    @Test
    void encuentraUsuarioPorEmail() {
        Usuario usuario = usuarioValido();
        when(repositorio.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertSame(usuario, servicio.obtenerUsuarioPorEmail(usuario.getEmail()).orElseThrow());
    }

    @Test
    void actualizaDatosDeContactoYConservaCredenciales() {
        Usuario usuario = usuarioValido();
        Usuario cambios = Usuario.builder().nombre("Ana").apellido("Rojas")
                .telefono("123").direccion("Avenida Sur").email("otro@example.com")
                .password("otra").build();
        when(repositorio.findById(1L)).thenReturn(Optional.of(usuario));
        when(repositorio.save(usuario)).thenReturn(usuario);

        Usuario resultado = servicio.actualizarUsuario(1L, cambios);

        assertEquals("Ana", resultado.getNombre());
        assertEquals("Rojas", resultado.getApellido());
        assertEquals("123", resultado.getTelefono());
        assertEquals("Avenida Sur", resultado.getDireccion());
        assertEquals("cliente@example.com", resultado.getEmail());
        assertEquals("clave-ejemplo", resultado.getPassword());
        verify(repositorio).save(usuario);
    }

    @Test
    void rechazaActualizarUsuarioInexistente() {
        when(repositorio.findById(1L)).thenReturn(Optional.empty());

        assertEquals("Usuario no encontrado", assertThrows(IllegalArgumentException.class,
                () -> servicio.actualizarUsuario(1L, usuarioValido())).getMessage());
        verify(repositorio, never()).save(any());
    }

    @Test
    void listaUsuariosDelRepositorio() {
        List<Usuario> usuarios = List.of(usuarioValido());
        when(repositorio.findAll()).thenReturn(usuarios);

        assertEquals(usuarios, servicio.listarUsuarios());
    }

    @Test
    void eliminaUsuarioPorId() {
        servicio.eliminarUsuario(1L);

        verify(repositorio).deleteById(1L);
    }

    private Usuario usuarioValido() {
        return Usuario.builder().id(1L).nombre("Juan").apellido("Pérez")
                .email("cliente@example.com").direccion("Calle Norte")
                .password("clave-ejemplo").rol(Rol.CLIENTE).build();
    }
}
