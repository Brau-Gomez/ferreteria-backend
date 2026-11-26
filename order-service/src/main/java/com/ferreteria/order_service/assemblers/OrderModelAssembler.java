package com.ferreteria.order_service.assemblers;

import com.ferreteria.order_service.controller.OrderControllerV2;
import com.ferreteria.order_service.dto.OrderResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<OrderResponseDTO, EntityModel<OrderResponseDTO>> {

    @Override
    public EntityModel<OrderResponseDTO> toModel(OrderResponseDTO order) {
        return EntityModel.of(
                order,
                linkTo(methodOn(OrderControllerV2.class).buscarPorId(order.getId(), null)).withSelfRel(),
                linkTo(methodOn(OrderControllerV2.class)
                        .listar(0, 20, "createdAt", "desc", null, null)).withRel("orders")
        );
    }
}
