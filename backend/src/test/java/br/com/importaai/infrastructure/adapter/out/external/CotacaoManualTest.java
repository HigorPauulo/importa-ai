package br.com.importaai.infrastructure.adapter.out.external;

import br.com.importaai.domain.model.Moeda;
import br.com.importaai.domain.model.PerfilUsuario;
import br.com.importaai.domain.port.in.ConsultarCotacaoUseCase;
import br.com.importaai.domain.port.in.DefinirCotacaoManualUseCase;
import br.com.importaai.domain.port.out.CambioPort;
import br.com.importaai.domain.port.out.CotacaoRepository;
import br.com.importaai.infrastructure.adapter.in.messaging.MessagingIntegrationTest;
import br.com.importaai.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.CotacaoCacheJpaRepository;
import br.com.importaai.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class CotacaoManualTest extends MessagingIntegrationTest {

    @MockitoBean CambioPort cambioPort;

    @Autowired DefinirCotacaoManualUseCase definirManual;
    @Autowired ConsultarCotacaoUseCase consultar;
    @Autowired CotacaoRepository cotacaoRepository;
    @Autowired CotacaoCacheJpaRepository cotacaoJpaRepository;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;

    private Long adminId;

    @BeforeEach
    void limpar() {
        cotacaoJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        UsuarioEntity admin = new UsuarioEntity();
        admin.setNome("Admin");
        admin.setEmail("admin@x.com");
        admin.setSenhaHash("hash");
        admin.setPerfil(PerfilUsuario.ADMINISTRADOR);
        admin.setCriadoEm(Instant.now());
        adminId = usuarioJpaRepository.save(admin).getId();
    }

    @Test
    @DisplayName("TC28: cotacao manual sobrescreve a automatica e e marcada como manual")
    void tc28_manualSobrescreveAutomatica() {
        // cotacao automatica previa para USD-BRL
        cotacaoRepository.salvar(
                br.com.importaai.domain.model.Cotacao.automatica(
                        Moeda.USD, Moeda.BRL, new BigDecimal("5.00"),
                        java.time.Instant.now(), java.time.Instant.now()));

        // admin define manual para o mesmo par
        definirManual.executar(new DefinirCotacaoManualUseCase.Input(
                Moeda.USD, Moeda.BRL, new BigDecimal("5.55"), adminId, null));

        // consulta deve refletir a manual (prevalece sobre a automatica)
        var out = consultar.executar(new ConsultarCotacaoUseCase.Input(Moeda.USD, Moeda.BRL)).orElseThrow();

        assertThat(out.cotacao().isManual()).isTrue();
        assertThat(out.cotacao().taxa()).isEqualByComparingTo("5.55");
        assertThat(out.desatualizada()).isFalse();

        // upsert por par: a manual sobrescreveu a automatica, ainda ha 1 linha para USD-BRL
        assertThat(cotacaoRepository.buscarPorPar(Moeda.USD, Moeda.BRL).orElseThrow().isManual()).isTrue();
    }
}
