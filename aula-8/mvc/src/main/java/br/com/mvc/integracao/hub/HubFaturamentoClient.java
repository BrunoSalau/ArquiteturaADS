package br.com.mvc.integracao.hub;

import br.com.mvc.model.Pedido;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Cliente REST do hub de faturamento (projeto da aula 7, porta 8081). */
@Component
public class HubFaturamentoClient {

  private static final Logger log = LoggerFactory.getLogger(HubFaturamentoClient.class);

  private final RestClient restClient;

  public HubFaturamentoClient(@Value("${hub.faturamento.url}") String urlBase,
      @Value("${hub.faturamento.timeout-ms:5000}") long timeoutMs) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofMillis(timeoutMs));
    requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

    this.restClient = RestClient.builder()
        .baseUrl(urlBase)
        .requestFactory(requestFactory)
        .build();
  }

  /** Envia o pedido ao hub e devolve o ID da fatura criada. */
  public String faturar(Pedido pedido) {
    FaturamentoRequest request = FaturamentoRequest.de(pedido);
    log.info("Enviando pedido #{} ao hub de faturamento", pedido.getId());
    try {
      FaturamentoResponse response = restClient.post()
          .uri("/faturas")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .body(FaturamentoResponse.class);

      if (response == null || response.faturaId() == null || response.faturaId().isBlank()) {
        throw new HubFaturamentoException("O hub respondeu sem o ID da fatura.");
      }
      try {
        UUID.fromString(response.faturaId());
      } catch (IllegalArgumentException e) {
        throw new HubFaturamentoException("O hub respondeu com um ID de fatura inválido.", e);
      }
      log.info("Pedido #{} faturado no hub. Fatura {}", pedido.getId(), response.faturaId());
      return response.faturaId();

    } catch (RestClientResponseException e) {
      throw new HubFaturamentoException("O hub recusou o pedido (HTTP " + e.getStatusCode().value()
          + "). Consulte o hub antes de reenviar.", e);
    } catch (RestClientException e) {
      throw new HubFaturamentoException("Hub de faturamento indisponível: " + e.getMessage(), e);
    }
  }
}
