package br.com.hub.application.port.in;

import br.com.hub.domain.model.Pedido;
import java.util.UUID;

/** Porta de entrada: faturar um pedido e devolver o ID da fatura. */
public interface FaturarPedidoUseCase {

  UUID faturar(Pedido pedido);
}
