package com.ferreteria.catalog_service.service;

import com.ferreteria.catalog_service.exception.BadRequestException;
import com.ferreteria.catalog_service.exception.ResourceNotFoundException;
import com.ferreteria.catalog_service.model.Region;
import com.ferreteria.catalog_service.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    @Mock
    private RegionRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RegionService service;

    @Test
    void listarRegionesActivas() {
        when(repository.findByIsActiveTrueOrderByDisplayOrderAscNameAsc())
                .thenReturn(List.of(region()));

        assertThat(service.findAllActive())
                .hasSize(1)
                .extracting(Region::getCode)
                .containsExactly("RM");
    }

    @Test
    void buscarPorIdExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(region()));

        assertThat(service.findById(1L).getCode()).isEqualTo("RM");
    }

    @Test
    void buscarPorIdInexistenteLanzaError() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crearRegionNueva() {
        Region nueva = region();
        nueva.setCode("VI");
        when(repository.existsByCode("VI")).thenReturn(false);
        when(repository.save(any(Region.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.create(nueva).getCode()).isEqualTo("VI");
        verify(repository).save(nueva);
    }

    @Test
    void crearRegionDuplicadaLanzaError() {
        Region nueva = region();
        nueva.setCode("RM");
        when(repository.existsByCode("RM")).thenReturn(true);

        assertThatThrownBy(() -> service.create(nueva))
                .isInstanceOf(BadRequestException.class);
    }

    private Region region() {
        return Region.builder()
                .id(1L)
                .code("RM")
                .name("Región Metropolitana")
                .isActive(true)
                .displayOrder(1)
                .build();
    }
}
