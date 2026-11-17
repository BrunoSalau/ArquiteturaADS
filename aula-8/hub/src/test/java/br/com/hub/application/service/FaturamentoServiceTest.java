package br.com.hub.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.domain.exception.RegraFaturamentoException;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.ItemPedido;
import br.com.hub.domain.model.Pedido;
import br.com.hub.domain.model.StatusFatura;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Teste do núcleo sem Spring: as portas de saída são trocadas por
 * implementações em memória, mostrando a vantagem da arquitetura hexagonal.
 */
class FaturamentoServiceTest {

  private final Map<UUID, Fatura> banco = new HashMap<>();

  private final FaturaRepositoryPort repositorioEmMemoria = new FaturaRepositoryPort() {
    @Override
    public Fatura salvar(Fatura fatura) {
      banco.put(fatura.getId(), fatura);
      return fatura;
    }

    @Override
    public Optional<Fatura> buscarPorId(UUID id) {
      return Optional.ofNullable(banco.get(id));
    }
  };

  private final Pedido pedido = new Pedido(1L, "Maria", "maria@email.com", new BigDecimal("100.00"),
      List.of(new ItemPedido("Teclado", 1, new BigDecimal("100.00"))));

  @Test
  void deveCriarFaturaEGuardarCodigoBarrasChaveEHash() {
    FaturamentoService service = new FaturamentoService(repositorioEmMemoria,
        f -> "0".repeat(44),
        (f, p) -> "1".repeat(44),
        (f, p) -> "a".repeat(64));

    UUID id = service.faturar(pedido);

    Fatura fatura = service.buscar(id).orElseThrow();
    assertEquals(StatusFatura.FATURADA, fatura.getStatus());
    assertEquals(44, fatura.getCodigoBarrasBoleto().length());
    assertEquals(44, fatura.getChaveAcessoNotaFiscal().length());
    assertEquals(64, fatura.getHashEmail().length());
  }

  @Test
  void deveRejeitarChaveDeAcessoQueNaoTem44Digitos() {
    FaturamentoService service = new FaturamentoService(repositorioEmMemoria,
        f -> "0".repeat(44),
        (f, p) -> "123",
        (f, p) -> "a".repeat(64));

    assertThrows(RegraFaturamentoException.class, () -> service.faturar(pedido));
    assertEquals(1, banco.size());
    assertTrue(banco.values().stream().allMatch(f -> f.getStatus() == StatusFatura.BOLETO_EMITIDO));
  }
}
