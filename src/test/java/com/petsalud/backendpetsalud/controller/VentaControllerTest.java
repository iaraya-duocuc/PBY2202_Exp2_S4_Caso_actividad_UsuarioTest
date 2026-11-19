package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class VentaControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UsuarioRepository usuarios;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "CLIENTE"})
    void permiteRolesAutorizadosYRechazaUsuarioAsociadoSinDireccion(String rol) throws Exception {
        Usuario incompleto = Usuario.builder().id(1L).nombre("Ana").apellido("Rojas")
                .email("ana@example.com").rol(Rol.CLIENTE).build();
        when(usuarios.findById(1L)).thenReturn(Optional.of(incompleto));

        mvc.perform(post("/api/ventas")
                        .with(user("operador").authorities(() -> rol))
                        .with(csrf())
                        .param("usuarioId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[10]"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("La dirección del usuario es obligatoria"));
        verify(usuarios).findById(1L);
    }

    @Test
    void deniegaRegistroDeVentaAlVeterinario() throws Exception {
        mvc.perform(post("/api/ventas")
                        .with(user("veterinario").authorities(() -> "VETERINARIO"))
                        .with(csrf())
                        .param("usuarioId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[10]"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(usuarios);
    }
}
