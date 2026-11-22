package com.ferreteria.catalog_service.controller;

import com.ferreteria.catalog_service.assemblers.ComunaModelAssembler;
import com.ferreteria.catalog_service.dto.ComunaResponseDTO;
import com.ferreteria.catalog_service.service.ComunaService;
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
@RequestMapping("/communes/v2")
@RequiredArgsConstructor
public class ComunaControllerV2 {

    private final ComunaService comunaService;
    private final ComunaModelAssembler assembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EntityModel<ComunaResponseDTO>> listar() {
        List<EntityModel<ComunaResponseDTO>> communes = comunaService.findAllActive().stream()
                .map(ComunaResponseDTO::fromEntity)
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(communes, linkTo(methodOn(ComunaControllerV2.class).listar()).withSelfRel());
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<ComunaResponseDTO> buscarPorId(@PathVariable Long id) {
        return assembler.toModel(ComunaResponseDTO.fromEntity(comunaService.findById(id)));
    }

    @GetMapping(value = "/region/{regionId}", produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EntityModel<ComunaResponseDTO>> listarPorRegion(@PathVariable Long regionId) {
        List<EntityModel<ComunaResponseDTO>> communes = comunaService.findByRegionId(regionId).stream()
                .map(ComunaResponseDTO::fromEntity)
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(
                communes,
                linkTo(methodOn(ComunaControllerV2.class).listarPorRegion(regionId)).withSelfRel()
        );
    }
}
