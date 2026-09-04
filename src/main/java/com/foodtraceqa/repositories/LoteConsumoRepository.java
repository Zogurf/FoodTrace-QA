package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.LoteConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoteConsumoRepository extends JpaRepository<LoteConsumo, Long> {
    List<LoteConsumo> findByLoteProducidoId(Long loteProducidoId);
    List<LoteConsumo> findByLoteEntranteId(Long loteEntranteId);
}
