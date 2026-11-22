package com.ferreteria.product_service.service;

import com.ferreteria.product_service.dto.PageResponseDTO;
import com.ferreteria.product_service.dto.ProductImageRequestDTO;
import com.ferreteria.product_service.dto.ProductImageResponseDTO;
import com.ferreteria.product_service.dto.ProductRequestDTO;
import com.ferreteria.product_service.dto.ProductResponseDTO;
import com.ferreteria.product_service.exception.BadRequestException;
import com.ferreteria.product_service.exception.ResourceNotFoundException;
import com.ferreteria.product_service.event.ProductEvents;
import com.ferreteria.product_service.model.Category;
import com.ferreteria.product_service.model.Product;
import com.ferreteria.product_service.model.ProductImage;
import com.ferreteria.product_service.repository.CategoryRepository;
import com.ferreteria.product_service.repository.ProductImageRepository;
import com.ferreteria.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductResponseDTO crear(ProductRequestDTO request) {
        if (productRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Ya existe un producto con ese código");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoryId()));

        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .costPrice(request.getCostPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .minStock(request.getMinStock() != null ? request.getMinStock() : 0)
                .maxStock(request.getMaxStock())
                .weightKg(request.getWeightKg())
                .active(request.getActive() != null ? request.getActive() : true)
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .category(category)
                .build();

        Product guardado = productRepository.save(product);
        log.info("Producto creado id={} code={}", guardado.getId(), guardado.getCode());
        ProductResponseDTO response = ProductResponseDTO.fromEntity(guardado);
        eventPublisher.publishEvent(ProductEvents.ProductCreatedEvent.of(guardado));
        return response;
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO obtenerPorId(UUID id) {
        return ProductResponseDTO.fromEntity(obtenerProducto(id));
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO obtenerPorCode(String code) {
        Product product = productRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código " + code));
        return ProductResponseDTO.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductResponseDTO> listar(int page, int size, String sortBy, String sortDir, String filtro, UUID categoryId, Boolean soloActivas, BigDecimal minPrice, BigDecimal maxPrice) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Product> productosPage;
        if (filtro != null && !filtro.isBlank()) {
            productosPage = productRepository.findByFiltro(filtro.trim(), pageable);
        } else if (categoryId != null) {
            productosPage = productRepository.findByCategoryId(categoryId, pageable);
        } else if (minPrice != null && maxPrice != null) {
            productosPage = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        } else if (Boolean.TRUE.equals(soloActivas)) {
            productosPage = productRepository.findAllActive(pageable);
        } else {
            productosPage = productRepository.findAll(pageable);
        }

        return PageResponseDTO.from(productosPage.map(ProductResponseDTO::fromEntityLite));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listarDestacados() {
        return productRepository.findFeatured().stream()
                .map(ProductResponseDTO::fromEntityLite)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listarStockBajo() {
        return productRepository.findLowStock().stream()
                .map(ProductResponseDTO::fromEntityLite)
                .toList();
    }

    public ProductResponseDTO actualizar(UUID id, ProductRequestDTO request) {
        Product product = obtenerProducto(id);

        if (!product.getCode().equals(request.getCode()) && productRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Ya existe un producto con ese código");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoryId()));

        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCompareAtPrice(request.getCompareAtPrice());
        product.setCostPrice(request.getCostPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getMinStock() != null) product.setMinStock(request.getMinStock());
        product.setMaxStock(request.getMaxStock());
        product.setWeightKg(request.getWeightKg());
        if (request.getActive() != null) product.setActive(request.getActive());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());
        product.setCategory(category);

        Product actualizado = productRepository.save(product);
        log.info("Producto actualizado id={}", id);
        ProductResponseDTO response = ProductResponseDTO.fromEntity(actualizado);
        eventPublisher.publishEvent(ProductEvents.ProductUpdatedEvent.of(actualizado));
        return response;
    }

    public ProductResponseDTO actualizarStock(UUID id, int cantidad) {
        Product product = obtenerProducto(id);
        int previousStock = product.getStock();
        int nuevoStock = product.getStock() + cantidad;
        if (nuevoStock < 0) {
            throw new BadRequestException("Stock insuficiente. Stock actual: " + product.getStock());
        }
        product.setStock(nuevoStock);
        log.info("Stock actualizado producto id={} nuevoStock={}", id, nuevoStock);
        Product actualizado = productRepository.save(product);
        eventPublisher.publishEvent(ProductEvents.ProductStockChangedEvent.of(actualizado, previousStock));
        return ProductResponseDTO.fromEntity(actualizado);
    }

    public ProductResponseDTO actualizarEstado(UUID id, boolean active) {
        Product product = obtenerProducto(id);
        product.setActive(active);
        log.info("Estado de producto actualizado id={} active={}", id, active);
        Product actualizado = productRepository.save(product);
        eventPublisher.publishEvent(ProductEvents.ProductUpdatedEvent.of(actualizado));
        return ProductResponseDTO.fromEntity(actualizado);
    }

    public void eliminar(UUID id) {
        if (!productRepository.existsById(id)) {
            log.warn("Intento de eliminar producto inexistente id={}", id);
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
        log.info("Producto eliminado id={}", id);
        eventPublisher.publishEvent(ProductEvents.ProductDeletedEvent.of(id));
    }

    @Transactional(readOnly = true)
    public boolean existePorId(UUID id) {
        return productRepository.existsById(id);
    }

    // Gestión de imágenes
    public ProductImageResponseDTO agregarImagen(UUID productId, ProductImageRequestDTO request) {
        Product product = obtenerProducto(productId);

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(request.getUrl())
                .altText(request.getAltText())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .primary(request.getPrimary() != null ? request.getPrimary() : false)
                .build();

        // Si es primary, desactivar las otras
        if (Boolean.TRUE.equals(image.isPrimary())) {
            imageRepository.findByProductIdAndPrimaryTrue(productId)
                    .ifPresent(existing -> {
                        existing.setPrimary(false);
                        imageRepository.save(existing);
                    });
        }

        ProductImage guardada = imageRepository.save(image);
        log.info("Imagen agregada producto id={} imageId={}", productId, guardada.getId());
        return ProductImageResponseDTO.fromEntity(guardada);
    }

    public void eliminarImagen(UUID imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Imagen no encontrada con ID: " + imageId);
        }
        imageRepository.deleteById(imageId);
        log.info("Imagen eliminada id={}", imageId);
    }

    private Product obtenerProducto(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }
}