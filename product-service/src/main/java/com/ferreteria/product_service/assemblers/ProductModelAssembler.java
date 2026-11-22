package com.ferreteria.product_service.assemblers;

import com.ferreteria.product_service.controller.ProductControllerV2;
import com.ferreteria.product_service.dto.ProductResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductModelAssembler
        implements RepresentationModelAssembler<ProductResponseDTO, EntityModel<ProductResponseDTO>> {

    @Override
    public EntityModel<ProductResponseDTO> toModel(ProductResponseDTO product) {
        return EntityModel.of(
                product,
                linkTo(methodOn(ProductControllerV2.class).buscarPorId(product.getId())).withSelfRel(),
                linkTo(methodOn(ProductControllerV2.class)
                        .listar(0, 20, "createdAt", "desc", null, null, true, null, null)).withRel("products"),
                linkTo(methodOn(ProductControllerV2.class).buscarPorCode(product.getCode())).withRel("code"),
                linkTo(methodOn(ProductControllerV2.class).listarDestacados()).withRel("featured")
        );
    }
}
