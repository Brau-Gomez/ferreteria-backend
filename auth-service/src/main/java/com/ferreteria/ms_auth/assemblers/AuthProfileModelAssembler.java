package com.ferreteria.ms_auth.assemblers;

import com.ferreteria.ms_auth.controller.AuthControllerV2;
import com.ferreteria.ms_auth.dto.UsuarioPerfilDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AuthProfileModelAssembler
        implements RepresentationModelAssembler<UsuarioPerfilDTO, EntityModel<UsuarioPerfilDTO>> {

    @Override
    public EntityModel<UsuarioPerfilDTO> toModel(UsuarioPerfilDTO profile) {
        return EntityModel.of(
                profile,
                linkTo(methodOn(AuthControllerV2.class).me(null)).withSelfRel(),
                linkTo(methodOn(AuthControllerV2.class).validate(null)).withRel("validate-token")
        );
    }
}
