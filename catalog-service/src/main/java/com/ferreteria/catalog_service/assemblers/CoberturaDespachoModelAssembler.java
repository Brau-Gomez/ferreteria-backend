package com.ferreteria.catalog_service.assemblers;

import com.ferreteria.catalog_service.controller.CoberturaDespachoControllerV2;
import com.ferreteria.catalog_service.dto.CoberturaDespachoResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CoberturaDespachoModelAssembler
        implements RepresentationModelAssembler<CoberturaDespachoResponseDTO, EntityModel<CoberturaDespachoResponseDTO>> {

    @Override
    public EntityModel<CoberturaDespachoResponseDTO> toModel(CoberturaDespachoResponseDTO coverage) {
        return EntityModel.of(
                coverage,
                linkTo(methodOn(CoberturaDespachoControllerV2.class).buscarPorComuna(coverage.comunaId())).withSelfRel(),
                linkTo(methodOn(CoberturaDespachoControllerV2.class).listar()).withRel("coverage")
        );
    }
}
