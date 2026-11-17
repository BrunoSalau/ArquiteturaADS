package br.com.hub.adapter.in.rest;

import br.com.hub.application.port.in.ConsultarFaturaUseCase;
import br.com.hub.application.port.in.FaturarPedidoUseCase;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada (camada REST). Só traduz HTTP para o caso de uso. */
@RestController
@RequestMapping("/faturas")
public class FaturaController {

  private final FaturarPedidoUseCase faturarPedido;
  private final ConsultarFaturaUseCase consultarFatura;

  public FaturaController(FaturarPedidoUseCase faturarPedido, ConsultarFaturaUseCase consultarFatura) {
    this.faturarPedido = faturarPedido;
    this.consultarFatura = consultarFatura;
  }

  @PostMapping
  public ResponseEntity<FaturaCriadaResponse> faturar(@RequestBody PedidoRequest request) {
    UUID faturaId = faturarPedido.faturar(request.paraDominio());
    return ResponseEntity.created(URI.create("/faturas/" + faturaId))
        .body(new FaturaCriadaResponse(faturaId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<FaturaResponse> buscar(@PathVariable UUID id) {
    return consultarFatura.buscar(id)
        .map(FaturaResponse::de)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
