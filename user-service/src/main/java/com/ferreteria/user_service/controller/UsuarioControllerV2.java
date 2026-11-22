package com.ferreteria.user_service.controller;

import com.ferreteria.user_service.assemblers.UsuarioModelAssembler;
import com.ferreteria.user_service.dto.UsuarioResponseDTO;
import com.ferreteria.user_service.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping({"/users/v2", "/usuarios/v2"})
@RequiredArgsConstructor
public class UsuarioControllerV2 {

    private final UsuarioService usuarioService;
    private final UsuarioModelAssembler assembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public CollectionModel<EntityModel<UsuarioResponseDTO>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nombreCompleto") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String filtro) {
        List<EntityModel<UsuarioResponseDTO>> usuarios = usuarioService
                .listar(page, size, sortBy, sortDir, filtro)
                .getContent()
                .stream()
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(
                usuarios,
                linkTo(methodOn(UsuarioControllerV2.class).listar(page, size, sortBy, sortDir, filtro)).withSelfRel()
        );
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        return assembler.toModel(usuarioService.obtenerPorId(id));
    }

    @GetMapping(value = "/email/{email}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<UsuarioResponseDTO> buscarPorEmail(@PathVariable String email) {
        return assembler.toModel(usuarioService.obtenerPorEmail(email));
    }
}
