package com.ferreteria.catalog_service.service;

import com.ferreteria.catalog_service.exception.BadRequestException;
import com.ferreteria.catalog_service.exception.ResourceNotFoundException;
import com.ferreteria.catalog_service.event.CatalogEvents;
import com.ferreteria.catalog_service.model.Region;
import com.ferreteria.catalog_service.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public List<Region> findAllActive() {
        return repository.findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    public Region findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Región no encontrada: " + id));
    }

    public Region findByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Región no encontrada: " + code));
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Transactional
    public Region create(Region region) {
        if (repository.existsByCode(region.getCode())) {
            throw new BadRequestException("Código de región ya existe: " + region.getCode());
        }
        Region guardada = repository.save(region);
        eventPublisher.publishEvent(CatalogEvents.RegionCreatedEvent.of(guardada));
        return guardada;
    }

    @Transactional
    public Region update(Long id, Region data) {
        Region region = findById(id);
        region.setName(data.getName());
        region.setIsActive(data.getIsActive());
        region.setDisplayOrder(data.getDisplayOrder());
        Region actualizada = repository.save(region);
        eventPublisher.publishEvent(CatalogEvents.RegionUpdatedEvent.of(actualizada));
        return actualizada;
    }

    @Transactional
    public void delete(Long id) {
        Region region = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Región no encontrada: " + id));
        region.setIsActive(false);
        repository.save(region);
        eventPublisher.publishEvent(CatalogEvents.RegionDeletedEvent.of(id));
    }
}