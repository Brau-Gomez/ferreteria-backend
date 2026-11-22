package com.ferreteria.catalog_service.assemblers;

import com.ferreteria.catalog_service.controller.RegionControllerV2;
import com.ferreteria.catalog_service.dto.RegionResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RegionModelAssembler implements RepresentationModelAssembler<RegionResponseDTO, EntityModel<RegionResponseDTO>> {

    @Override
    public EntityModel<RegionResponseDTO> toModel(RegionResponseDTO region) {
        return EntityModel.of(
                region,
                linkTo(methodOn(RegionControllerV2.class).buscarPorId(region.id())).withSelfRel(),
                linkTo(methodOn(RegionControllerV2.class).listar()).withRel("regions")
        );
    }
}
