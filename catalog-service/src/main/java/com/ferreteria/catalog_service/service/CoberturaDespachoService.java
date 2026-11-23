package com.ferreteria.catalog_service.service;

import com.ferreteria.catalog_service.exception.ResourceNotFoundException;
import com.ferreteria.catalog_service.event.CatalogEvents;
import com.ferreteria.catalog_service.model.CoberturaDespacho;
import com.ferreteria.catalog_service.model.Comuna;
import com.ferreteria.catalog_service.repository.CoberturaDespachoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoberturaDespachoService {

    private final CoberturaDespachoRepository repository;
    private final ComunaService comunaService;
    private final ApplicationEventPublisher eventPublisher;

    public List<CoberturaDespacho> findAllActiveCovered() {
        return repository.findByIsCoveredTrueAndIsActiveTrue();
    }

    public Optional<CoberturaDespacho> findByComunaId(Long comunaId) {
        return repository.findByComunaId(comunaId);
    }

    public CoberturaDespacho getByComunaId(Long comunaId) {
        return repository.findByComunaId(comunaId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay cobertura configurada para la comuna: " + comunaId));
    }

    public boolean isCovered(Long comunaId) {
        return repository.findByComunaId(comunaId)
                .map(c -> c.getIsCovered() && c.getIsActive())
                .orElse(false);
    }

    public BigDecimal getDeliveryCost(Long comunaId) {
        return repository.findByComunaId(comunaId)
                .map(CoberturaDespacho::getDeliveryCost)
                .orElse(BigDecimal.ZERO);
    }

    public Integer getEstimatedDays(Long comunaId) {
        return repository.findByComunaId(comunaId)
                .map(CoberturaDespacho::getEstimatedDays)
                .orElse(0);
    }

    @Transactional
    public CoberturaDespacho createOrUpdate(Long comunaId, Boolean isCovered, BigDecimal deliveryCost,
                                             BigDecimal minOrderAmount, Integer estimatedDays) {
        Comuna comuna = comunaService.findById(comunaId);
        CoberturaDespacho coverage = repository.findByComunaId(comunaId)
                .orElse(CoberturaDespacho.builder().comuna(comuna).build());

        coverage.setIsCovered(isCovered);
        coverage.setDeliveryCost(deliveryCost);
        coverage.setMinOrderAmount(minOrderAmount);
        coverage.setEstimatedDays(estimatedDays);
        coverage.setIsActive(true);

        CoberturaDespacho guardada = repository.save(coverage);
        eventPublisher.publishEvent(CatalogEvents.CoberturaUpdatedEvent.of(guardada));
        return guardada;
    }

    @Transactional
    public void delete(Long comunaId) {
        CoberturaDespacho coverage = repository.findByComunaId(comunaId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay cobertura configurada para la comuna: " + comunaId));
        coverage.setIsCovered(false);
        coverage.setIsActive(false);
        repository.save(coverage);
        eventPublisher.publishEvent(CatalogEvents.CoberturaDeletedEvent.of(comunaId));
    }
}