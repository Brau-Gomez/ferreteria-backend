package com.ferreteria.user_service.assemblers;

import com.ferreteria.user_service.controller.UsuarioControllerV2;
import com.ferreteria.user_service.dto.UsuarioResponseDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioModelAssembler
        implements RepresentationModelAssembler<UsuarioResponseDTO, EntityModel<UsuarioResponseDTO>> {

    @Override
    public EntityModel<UsuarioResponseDTO> toModel(UsuarioResponseDTO usuario) {
        return EntityModel.of(
                usuario,
                linkTo(methodOn(UsuarioControllerV2.class).buscarPorId(usuario.getId())).withSelfRel(),
                linkTo(methodOn(UsuarioControllerV2.class)
                        .listar(0, 20, "nombreCompleto", "asc", null)).withRel("usuarios"),
                linkTo(methodOn(UsuarioControllerV2.class).buscarPorEmail(usuario.getEmail())).withRel("email")
        );
    }
}
