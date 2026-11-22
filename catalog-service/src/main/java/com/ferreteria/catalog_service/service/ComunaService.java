package com.ferreteria.catalog_service.service;

import com.ferreteria.catalog_service.exception.BadRequestException;
import com.ferreteria.catalog_service.exception.ResourceNotFoundException;
import com.ferreteria.catalog_service.event.CatalogEvents;
import com.ferreteria.catalog_service.model.Comuna;
import com.ferreteria.catalog_service.model.Region;
import com.ferreteria.catalog_service.repository.ComunaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComunaService {

    private final ComunaRepository repository;
    private final RegionService regionService;
    private final ApplicationEventPublisher eventPublisher;

    public List<Comuna> findAllActive() {
        return repository.findByIsActiveTrueOrderByNameAsc();
    }

    public List<Comuna> findByRegionId(Long regionId) {
        regionService.findById(regionId); // valida que existe
        return repository.findByRegionIdAndIsActiveTrueOrderByDisplayOrderAscNameAsc(regionId);
    }

    public Comuna findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comuna no encontrada: " + id));
    }

    public Comuna findByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Comuna no encontrada: " + code));
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Transactional
    public Comuna create(Comuna comuna) {
        if (repository.existsByCode(comuna.getCode())) {
            throw new BadRequestException("Código de comuna ya existe: " + comuna.getCode());
        }
        Region region = regionService.findById(comuna.getRegion().getId());
        comuna.setRegion(region);
        Comuna guardada = repository.save(comuna);
        eventPublisher.publishEvent(CatalogEvents.ComunaCreatedEvent.of(guardada));
        return guardada;
    }

    @Transactional
    public Comuna update(Long id, Comuna data) {
        Comuna comuna = findById(id);
        comuna.setName(data.getName());
        comuna.setIsActive(data.getIsActive());
        comuna.setDisplayOrder(data.getDisplayOrder());
        if (data.getRegion() != null && !data.getRegion().getId().equals(comuna.getRegion().getId())) {
            Region region = regionService.findById(data.getRegion().getId());
            comuna.setRegion(region);
        }
        Comuna actualizada = repository.save(comuna);
        eventPublisher.publishEvent(CatalogEvents.ComunaUpdatedEvent.of(actualizada));
        return actualizada;
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Comuna no encontrada: " + id);
        }
        repository.deleteById(id);
        eventPublisher.publishEvent(CatalogEvents.ComunaDeletedEvent.of(id));
    }
}