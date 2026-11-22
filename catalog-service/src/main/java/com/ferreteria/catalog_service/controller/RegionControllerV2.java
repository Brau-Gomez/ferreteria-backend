package com.ferreteria.catalog_service.controller;

import com.ferreteria.catalog_service.assemblers.RegionModelAssembler;
import com.ferreteria.catalog_service.dto.RegionResponseDTO;
import com.ferreteria.catalog_service.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/regions/v2")
@RequiredArgsConstructor
public class RegionControllerV2 {

    private final RegionService regionService;
    private final RegionModelAssembler assembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EntityModel<RegionResponseDTO>> listar() {
        List<EntityModel<RegionResponseDTO>> regions = regionService.findAllActive().stream()
                .map(RegionResponseDTO::fromEntity)
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(regions, linkTo(methodOn(RegionControllerV2.class).listar()).withSelfRel());
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<RegionResponseDTO> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(RegionResponseDTO.fromEntity(regionService.findById(id)));
    }
}
