package br.com.mvc;

import br.com.mvc.integracao.hub.HubFaturamentoClient;
import br.com.mvc.model.Pedido;
import br.com.mvc.model.Produto;
import br.com.mvc.model.StatusPedido;
import br.com.mvc.service.*;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:integracao;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PedidoIntegracaoTest {
    private static final String FATURA = UUID.randomUUID().toString();
    private static final AtomicInteger chamadas = new AtomicInteger();
    private static volatile int codigo = 201;
    private static volatile String resposta;
    private static volatile String recebido;
    private static volatile long atraso;
    private static final java.util.concurrent.ExecutorService executor = Executors.newCachedThreadPool();
    private static final HttpServer hub = criarServidor();

    private static HttpServer criarServidor() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(executor);
            server.createContext("/faturas", exchange -> {
                chamadas.incrementAndGet();
                recebido = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                int status = codigo;
                byte[] body = resposta.getBytes(StandardCharsets.UTF_8);
                try {
                    Thread.sleep(atraso);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(status, body.length);
                    exchange.getResponseBody().write(body);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                catch (IOException ignored) { /* Esperado quando o cliente expira o timeout. */ }
                finally { exchange.close(); }
            });
            server.start();
            return server;
        } catch (IOException e) { throw new IllegalStateException(e); }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("hub.faturamento.url", () -> "http://127.0.0.1:" + hub.getAddress().getPort());
        registry.add("hub.faturamento.timeout-ms", () -> 1000);
    }

    @Autowired PedidoService pedidos;
    @Autowired ProdutoService produtos;
    @Autowired EstoqueService estoque;
    @Autowired HubFaturamentoClient cliente;
    private Long produtoId;

    @BeforeEach void preparar() {
        codigo = 201;
        resposta = "{\"faturaId\":\"" + FATURA + "\"}";
        atraso = 0;
        chamadas.set(0);
        var produto = produtos.criar(new Produto("Produto teste", "Integração", new BigDecimal("25.50")));
        produtoId = produto.getId();
        estoque.registrarEntrada(produtoId, 10, "Teste");
    }

    private Pedido novoPedido() {
        var pedido = pedidos.criar("Cliente teste", "cliente@example.com");
        pedidos.adicionarItem(pedido.getId(), produtoId, 2);
        return pedido;
    }

    @Test void fechaPedidoEnviaContratoEGravaIdNaBase() {
        var pedido = novoPedido();
        pedidos.fechar(pedido.getId());
        var gravado = pedidos.buscar(pedido.getId());
        assertEquals(StatusPedido.FATURADO, gravado.getStatus());
        assertEquals(FATURA, gravado.getFaturaId());
        assertNull(gravado.getErroIntegracao());
        assertEquals(8, produtos.buscar(produtoId).getQuantidadeEstoque());
        var json = JsonMapper.builder().build().readTree(recebido);
        assertEquals(pedido.getId().longValue(), json.get("pedidoId").asLong());
        assertEquals("Cliente teste", json.get("cliente").asString());
        assertEquals("cliente@example.com", json.get("emailCliente").asString());
        assertEquals(0, new BigDecimal("51.00").compareTo(json.get("valorTotal").decimalValue()));
        assertEquals(2, json.get("itens").get(0).get("quantidade").asInt());
        assertEquals(1, chamadas.get());
        assertThrows(RegraNegocioException.class, () -> pedidos.fechar(pedido.getId()));
        assertEquals(1, chamadas.get());
    }

    @Test void falhaHttpPreservaPedidoEReenvioNaoRepeteBaixa() {
        var pedido = novoPedido();
        codigo = 503;
        resposta = "{\"erro\":\"indisponível\"}";
        pedidos.fechar(pedido.getId());
        var pendente = pedidos.buscar(pedido.getId());
        assertEquals(StatusPedido.FECHADO, pendente.getStatus());
        assertNull(pendente.getFaturaId());
        assertTrue(pendente.getErroIntegracao().contains("503"));
        assertEquals(8, produtos.buscar(produtoId).getQuantidadeEstoque());
        codigo = 201;
        resposta = "{\"faturaId\":\"" + FATURA + "\"}";
        pedidos.reenviarAoHub(pedido.getId());
        assertEquals(FATURA, pedidos.buscar(pedido.getId()).getFaturaId());
        assertEquals(8, produtos.buscar(produtoId).getQuantidadeEstoque());
        assertEquals(2, chamadas.get());
    }

    @Test void respostaSemIdNaoMarcaComoFaturado() {
        var pedido = novoPedido();
        resposta = "{}";
        pedidos.fechar(pedido.getId());
        assertTrue(pedidos.buscar(pedido.getId()).isAguardandoFatura());
        assertTrue(pedidos.buscar(pedido.getId()).getErroIntegracao().contains("sem o ID"));
    }

    @Test void respostaComIdInvalidoNaoMarcaComoFaturado() {
        var pedido = novoPedido();
        resposta = "{\"faturaId\":\"invalido\"}";
        pedidos.fechar(pedido.getId());
        assertTrue(pedidos.buscar(pedido.getId()).isAguardandoFatura());
        assertTrue(pedidos.buscar(pedido.getId()).getErroIntegracao().contains("inválido"));
    }

    @Test void timeoutPreservaPendenciaSemRetryAutomatico() {
        var pedido = novoPedido();
        atraso = 1800;
        pedidos.fechar(pedido.getId());
        assertTrue(pedidos.buscar(pedido.getId()).isAguardandoFatura());
        assertNotNull(pedidos.buscar(pedido.getId()).getErroIntegracao());
        assertEquals(1, chamadas.get());
    }

    @Test void pedidoVazioNaoBaixaEstoqueNemChamaHub() {
        var pedido = pedidos.criar("Sem itens", "sem@example.com");
        assertThrows(RegraNegocioException.class, () -> pedidos.fechar(pedido.getId()));
        assertTrue(pedidos.buscar(pedido.getId()).isAberto());
        assertEquals(10, produtos.buscar(produtoId).getQuantidadeEstoque());
        assertEquals(0, chamadas.get());
    }

    @AfterAll static void encerrar() { hub.stop(0); executor.shutdownNow(); }
}
