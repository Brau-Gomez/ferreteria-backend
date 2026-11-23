package com.ferreteria.product_service.service;

import com.ferreteria.product_service.dto.CategoryRequestDTO;
import com.ferreteria.product_service.dto.CategoryResponseDTO;
import com.ferreteria.product_service.dto.PageResponseDTO;
import com.ferreteria.product_service.exception.BadRequestException;
import com.ferreteria.product_service.exception.ResourceNotFoundException;
import com.ferreteria.product_service.model.Category;
import com.ferreteria.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponseDTO crear(CategoryRequestDTO request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Ya existe una categoría con ese nombre");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Category guardada = categoryRepository.save(category);
        log.info("Categoría creada id={} name={}", guardada.getId(), guardada.getName());
        return CategoryResponseDTO.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO obtenerPorId(UUID id) {
        return CategoryResponseDTO.fromEntity(obtenerCategoria(id));
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO obtenerPorNombre(String name) {
        Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con nombre " + name));
        return CategoryResponseDTO.fromEntity(category);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<CategoryResponseDTO> listar(int page, int size, String sortBy, String sortDir, String filtro, Boolean soloActivas) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Category> categoriasPage;
        if (filtro != null && !filtro.isBlank()) {
            if (Boolean.TRUE.equals(soloActivas)) {
                categoriasPage = categoryRepository.findAllActive(pageable).map(c -> c);
            } else {
                categoriasPage = categoryRepository.findByNameContainingIgnoreCase(filtro.trim(), pageable);
            }
        } else {
            if (Boolean.TRUE.equals(soloActivas)) {
                categoriasPage = categoryRepository.findAllActive(pageable);
            } else {
                categoriasPage = categoryRepository.findAll(pageable);
            }
        }

        return PageResponseDTO.from(categoriasPage.map(CategoryResponseDTO::fromEntity));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> listarActivas() {
        return categoryRepository.findAllActive().stream()
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    public CategoryResponseDTO actualizar(UUID id, CategoryRequestDTO request) {
        Category category = obtenerCategoria(id);

        if (!category.getName().equalsIgnoreCase(request.getName()) && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Ya existe una categoría con ese nombre");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getActive() != null) category.setActive(request.getActive());

        Category actualizada = categoryRepository.save(category);
        log.info("Categoría actualizada id={}", id);
        return CategoryResponseDTO.fromEntity(actualizada);
    }

    public void eliminar(UUID id) {
        Category category = obtenerCategoria(id);
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Categoría desactivada id={}", id);
    }

    private Category obtenerCategoria(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Set<String> allowed = Set.of("createdAt", "name", "code", "sortOrder", "active");
        String sortField = allowed.contains(sortBy) ? sortBy : "sortOrder";
        int boundedSize = Math.min(Math.max(size, 1), 100);
        int boundedPage = Math.max(page, 0);
        return PageRequest.of(boundedPage, boundedSize, Sort.by(direction, sortField));
    }
}