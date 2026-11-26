package com.ferreteria.cart_service.assemblers;

import com.ferreteria.cart_service.controller.CartControllerV2;
import com.ferreteria.cart_service.dto.CartResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CartModelAssembler implements RepresentationModelAssembler<CartResponseDTO, EntityModel<CartResponseDTO>> {

    @Override
    public EntityModel<CartResponseDTO> toModel(CartResponseDTO cart) {
        return EntityModel.of(
                cart,
                linkTo(methodOn(CartControllerV2.class).getCart(null)).withSelfRel()
        );
    }
}
