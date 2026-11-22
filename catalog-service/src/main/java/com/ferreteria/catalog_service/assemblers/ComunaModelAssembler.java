package com.ferreteria.catalog_service.assemblers;

import com.ferreteria.catalog_service.controller.ComunaControllerV2;
import com.ferreteria.catalog_service.dto.ComunaResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ComunaModelAssembler implements RepresentationModelAssembler<ComunaResponseDTO, EntityModel<ComunaResponseDTO>> {

    @Override
    public EntityModel<ComunaResponseDTO> toModel(ComunaResponseDTO comuna) {
        return EntityModel.of(
                comuna,
                linkTo(methodOn(ComunaControllerV2.class).buscarPorId(comuna.id())).withSelfRel(),
                linkTo(methodOn(ComunaControllerV2.class).listar()).withRel("communes"),
                linkTo(methodOn(ComunaControllerV2.class).listarPorRegion(comuna.regionId())).withRel("region")
        );
    }
}
