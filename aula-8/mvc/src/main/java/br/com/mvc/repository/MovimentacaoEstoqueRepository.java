package br.com.mvc.repository;

import br.com.mvc.model.MovimentacaoEstoque;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

  List<MovimentacaoEstoque> findAllByOrderByDataHoraDesc();
}
