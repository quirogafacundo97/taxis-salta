package com.unsa.taxis.service;

import com.unsa.taxis.dto.PropietarioDTO;
import com.unsa.taxis.dto.CrearPropietarioRequest;
import com.unsa.taxis.dto.ActualizarPropietarioRequest;
import com.unsa.taxis.exception.PropietarioNoEncontradoException;
import com.unsa.taxis.exception.DniPropietarioDuplicadoException;
import com.unsa.taxis.model.Propietario;
import com.unsa.taxis.repository.PropietarioRepository;
import com.unsa.taxis.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public PropietarioService(
            PropietarioRepository propietarioRepository,
            VehiculoRepository vehiculoRepository) {

        this.propietarioRepository = propietarioRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<PropietarioDTO> listarTodos() {

        return propietarioRepository.findAll()
                .stream()
                .map(propietario -> PropietarioDTO.builder()
                        .id(propietario.getId())
                        .nombre(propietario.getNombre())
                        .apellido(propietario.getApellido())
                        .dni(propietario.getDni())
                        .telefono(propietario.getTelefono())
                        .cantidadVehiculos(
                                vehiculoRepository.countByPropietario_Id(
                                        propietario.getId()
                                )
                        )
                        .build()
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public Propietario buscarPorId(Long id) {

        return propietarioRepository.findById(id)
                .orElseThrow(() ->
                        new PropietarioNoEncontradoException(id)
                );
    }

    @Transactional
    public Propietario crear(CrearPropietarioRequest request) {

        if (propietarioRepository.existsByDni(request.dni())) {
            throw new DniPropietarioDuplicadoException(request.dni());
        }

        Propietario propietario = Propietario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .dni(request.dni())
                .telefono(request.telefono())
                .build();

        return propietarioRepository.save(propietario);
    }

    @Transactional
    public Propietario actualizar(
            Long id,
            ActualizarPropietarioRequest request) {

        Propietario propietario = buscarPorId(id);

        propietarioRepository.findByDni(request.dni())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new DniPropietarioDuplicadoException(request.dni());
                });

        propietario.setNombre(request.nombre());
        propietario.setApellido(request.apellido());
        propietario.setDni(request.dni());
        propietario.setTelefono(request.telefono());

        return propietarioRepository.save(propietario);
    }
}