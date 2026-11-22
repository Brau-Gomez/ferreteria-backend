package com.ferreteria.product_service.controller;

import com.ferreteria.product_service.assemblers.ProductModelAssembler;
import com.ferreteria.product_service.dto.ProductResponseDTO;
import com.ferreteria.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/products/v2")
@RequiredArgsConstructor
public class ProductControllerV2 {

    private final ProductService productService;
    private final ProductModelAssembler assembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EntityModel<ProductResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean soloActivas,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<EntityModel<ProductResponseDTO>> products = productService
                .listar(page, size, sortBy, sortDir, filtro, categoryId, soloActivas, minPrice, maxPrice)
                .getContent()
                .stream()
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(
                products,
                linkTo(methodOn(ProductControllerV2.class)
                        .listar(page, size, sortBy, sortDir, filtro, categoryId, soloActivas, minPrice, maxPrice))
                        .withSelfRel()
        );
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<ProductResponseDTO> buscarPorId(@PathVariable UUID id) {
        return assembler.toModel(productService.obtenerPorId(id));
    }

    @GetMapping(value = "/code/{code}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<ProductResponseDTO> buscarPorCode(@PathVariable String code) {
        return assembler.toModel(productService.obtenerPorCode(code));
    }

    @GetMapping(value = "/featured", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EntityModel<ProductResponseDTO>> listarDestacados() {
        List<EntityModel<ProductResponseDTO>> products = productService.listarDestacados()
                .stream()
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(
                products,
                linkTo(methodOn(ProductControllerV2.class).listarDestacados()).withSelfRel()
        );
    }
}
