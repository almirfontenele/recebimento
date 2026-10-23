package com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence;

import com.fontecode.recebimento.recebimento.domain.model.Recebimento;
import com.fontecode.recebimento.recebimento.domain.port.out.RecebimentoRepositoryPort;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.entity.RecebimentoJpaEntity;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.mapper.RecebimentoPersistenceMapper;
import com.fontecode.recebimento.recebimento.infrastructure.adapter.out.persistence.repository.SpringDataRecebimentoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class RecebimentoPersistenceAdapter implements RecebimentoRepositoryPort {

    private final SpringDataRecebimentoRepository jpaRepository;
    private final RecebimentoPersistenceMapper mapper;

    public RecebimentoPersistenceAdapter(
            SpringDataRecebimentoRepository jpaRepository,
            RecebimentoPersistenceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Recebimento salvar(Recebimento recebimento) {
        RecebimentoJpaEntity entity = mapper.toEntity(recebimento);
        RecebimentoJpaEntity salvo = jpaRepository.save(entity);
        return mapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public List<Recebimento> salvarTodos(List<Recebimento> recebimentos) {
        List<RecebimentoJpaEntity> entities = recebimentos.stream()
                .map(mapper::toEntity)
                .toList();
        return jpaRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Recebimento> buscarPorId(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Recebimento> listarTodos() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void removerPorId(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal somarValorPorCpf(String cpf) {
        return jpaRepository.somarValorPorCpf(cpf);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal somarValorPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        return jpaRepository.somarValorPorPeriodo(dataInicial, dataFinal);
    }
}
