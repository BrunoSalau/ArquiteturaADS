package br.com.hub.application.port.out;

import br.com.hub.domain.model.Fatura;
import java.util.Optional;
import java.util.UUID;

/** Porta de saída: persistência da fatura. */
public interface FaturaRepositoryPort {

  Fatura salvar(Fatura fatura);

  Optional<Fatura> buscarPorId(UUID id);
}
