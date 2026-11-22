package com.ferreteria.catalog_service.controller;

import com.ferreteria.catalog_service.assemblers.CoberturaDespachoModelAssembler;
import com.ferreteria.catalog_service.dto.CoberturaDespachoResponseDTO;
import com.ferreteria.catalog_service.service.CoberturaDespachoService;
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
@RequestMapping("/coverage/v2")
@RequiredArgsConstructor
public class CoberturaDespachoControllerV2 {

    private final CoberturaDespachoService coverageService;
    private final CoberturaDespachoModelAssembler assembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EntityModel<CoberturaDespachoResponseDTO>> listar() {
        List<EntityModel<CoberturaDespachoResponseDTO>> coverage = coverageService.findAllActiveCovered().stream()
                .map(CoberturaDespachoResponseDTO::fromEntity)
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(coverage, linkTo(methodOn(CoberturaDespachoControllerV2.class).listar()).withSelfRel());
    }

    @GetMapping(value = "/comuna/{comunaId}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<CoberturaDespachoResponseDTO> buscarPorComuna(@PathVariable Long comunaId) {
        return assembler.toModel(
                CoberturaDespachoResponseDTO.fromEntity(coverageService.getByComunaId(comunaId))
        );
    }
}
