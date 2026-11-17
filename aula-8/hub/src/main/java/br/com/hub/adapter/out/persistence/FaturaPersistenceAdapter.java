package br.com.hub.adapter.out.persistence;

import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.domain.model.Fatura;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador de saída: implementa a porta de persistência usando JPA + H2. */
@Component
public class FaturaPersistenceAdapter implements FaturaRepositoryPort {

  private final FaturaJpaRepository jpaRepository;

  public FaturaPersistenceAdapter(FaturaJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Fatura salvar(Fatura fatura) {
    return paraDominio(jpaRepository.save(paraEntidade(fatura)));
  }

  @Override
  public Optional<Fatura> buscarPorId(UUID id) {
    return jpaRepository.findById(id).map(FaturaPersistenceAdapter::paraDominio);
  }

  private static FaturaEntity paraEntidade(Fatura f) {
    return new FaturaEntity(f.getId(), f.getPedidoId(), f.getCliente(), f.getEmailCliente(),
        f.getValor(), f.getCriadaEm(), f.getStatus(), f.getCodigoBarrasBoleto(),
        f.getChaveAcessoNotaFiscal(), f.getHashEmail());
  }

  private static Fatura paraDominio(FaturaEntity e) {
    return new Fatura(e.getId(), e.getPedidoId(), e.getCliente(), e.getEmailCliente(), e.getValor(),
        e.getCriadaEm(), e.getStatus(), e.getCodigoBarrasBoleto(), e.getChaveAcessoNotaFiscal(),
        e.getHashEmail());
  }
}
