package br.com.hub.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaturaJpaRepository extends JpaRepository<FaturaEntity, UUID> {
}
