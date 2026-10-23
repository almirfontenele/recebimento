package com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.mapper;

import com.fontecode.recebimento.recebimento.domain.model.Recebimento;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.entity.RecebimentoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RecebimentoPersistenceMapper {

    public RecebimentoJpaEntity toEntity(Recebimento domain) {
        if (domain == null) {
            return null;
        }
        return new RecebimentoJpaEntity(
                domain.getId(),
                domain.getCpf(),
                domain.getE2e(),
                domain.getData(),
                domain.getValor()
        );
    }

    public Recebimento toDomain(RecebimentoJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Recebimento(
                entity.getId(),
                entity.getCpf(),
                entity.getE2e(),
                entity.getData(),
                entity.getValor()
        );
    }
}
