package com.unsa.taxis.mapper;

import com.unsa.taxis.dto.ViajeResponse;
import com.unsa.taxis.model.Viaje;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ViajeMapper {

    @Mapping(source = "cliente.nombre", target = "clienteNombre")
    @Mapping(source = "chofer.id", target = "choferId")
    @Mapping(source = "chofer.nombre", target = "choferNombre")
    @Mapping(source = "chofer.apellido", target = "choferApellido")
    ViajeResponse toResponse(Viaje viaje);
}