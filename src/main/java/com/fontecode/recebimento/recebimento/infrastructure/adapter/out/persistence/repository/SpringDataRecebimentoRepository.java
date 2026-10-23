package com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.repository;

import com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.entity.RecebimentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface SpringDataRecebimentoRepository extends JpaRepository<RecebimentoJpaEntity, Long> {

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM RecebimentoJpaEntity r WHERE r.cpf = :cpf")
    BigDecimal somarValorPorCpf(@Param("cpf") String cpf);

    @Query("SELECT COALESCE(SUM(r.valor), 0) FROM RecebimentoJpaEntity r WHERE r.data BETWEEN :dataInicial AND :dataFinal")
    BigDecimal somarValorPorPeriodo(
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal
    );
}
