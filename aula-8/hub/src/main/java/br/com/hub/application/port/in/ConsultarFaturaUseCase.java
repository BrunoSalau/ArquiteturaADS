package br.com.hub.application.port.in;

import br.com.hub.domain.model.Fatura;
import java.util.Optional;
import java.util.UUID;

/** Porta de entrada: consultar uma fatura pelo ID. */
public interface ConsultarFaturaUseCase {

  Optional<Fatura> buscar(UUID faturaId);
}
