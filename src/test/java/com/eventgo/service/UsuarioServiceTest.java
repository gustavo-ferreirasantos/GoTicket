package com.eventgo.service;

import com.eventgo.dao.UsuarioDAO;
import com.eventgo.dto.UsuarioDTO;
import com.eventgo.model.Usuario;
import com.eventgo.model.enums.Perfil;
import com.eventgo.util.SenhaUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando usuário e senha conferem")
    void deveAutenticarComSucesso() throws SQLException {
        String hash = SenhaUtil.hashSenha("senha123");
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Admin");
        usuario.setLogin("admin");
        usuario.setSenhaHash(hash);
        usuario.setPerfil(Perfil.ADMIN);
        usuario.setAtivo(true);

        when(usuarioDAO.buscarPorLogin("admin")).thenReturn(Optional.of(usuario));

        Usuario autenticado = usuarioService.autenticar("admin", "senha123");

        assertNotNull(autenticado);
        assertEquals("admin", autenticado.getLogin());
        assertEquals(Perfil.ADMIN, autenticado.getPerfil());
    }

    @Test
    @DisplayName("Deve rejeitar autenticação se a senha estiver incorreta")
    void deveRejeitarSenhaIncorreta() throws SQLException {
        String hash = SenhaUtil.hashSenha("senha123");
        Usuario usuario = new Usuario();
        usuario.setLogin("admin");
        usuario.setSenhaHash(hash);
        usuario.setAtivo(true);

        when(usuarioDAO.buscarPorLogin("admin")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () ->
                usuarioService.autenticar("admin", "senhaErrada"));
    }

    @Test
    @DisplayName("Deve impedir autenticação de usuário inativo")
    void deveImpedirAutenticacaoUsuarioInativo() throws SQLException {
        String hash = SenhaUtil.hashSenha("senha123");
        Usuario usuario = new Usuario();
        usuario.setLogin("admin");
        usuario.setSenhaHash(hash);
        usuario.setAtivo(false);

        when(usuarioDAO.buscarPorLogin("admin")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalStateException.class, () ->
                usuarioService.autenticar("admin", "senha123"));
    }

    @Test
    @DisplayName("Deve cadastrar novo usuário com senha criptografada em BCrypt")
    void deveCadastrarUsuarioComHash() throws SQLException {
        UsuarioDTO dto = new UsuarioDTO("Operador 1", "op1", "senha123", Perfil.OPERADOR_BILHETERIA);

        when(usuarioDAO.buscarPorLogin("op1")).thenReturn(Optional.empty());
        when(usuarioDAO.inserir(any(Usuario.class))).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            u.setId(10L);
            return u;
        });

        Usuario criado = usuarioService.cadastrar(dto);

        assertNotNull(criado);
        assertEquals("op1", criado.getLogin());
        assertTrue(SenhaUtil.verificarSenha("senha123", criado.getSenhaHash()));
        verify(usuarioDAO, times(1)).inserir(any(Usuario.class));
    }
}
