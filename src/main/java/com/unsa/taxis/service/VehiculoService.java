package com.unsa.taxis.service;

import com.unsa.taxis.dto.ActualizarVehiculoRequest;
import com.unsa.taxis.dto.CrearVehiculoRequest;
import com.unsa.taxis.dto.VehiculoDTO;
import com.unsa.taxis.exception.LicenciaVehiculoDuplicadaException;
import com.unsa.taxis.exception.NroRelojFullmarDuplicadoException;
import com.unsa.taxis.exception.PatenteVehiculoDuplicadaException;
import com.unsa.taxis.exception.PropietarioNoEncontradoException;
import com.unsa.taxis.exception.VehiculoNoEncontradoException;
import com.unsa.taxis.model.Propietario;
import com.unsa.taxis.model.Vehiculo;
import com.unsa.taxis.repository.PropietarioRepository;
import com.unsa.taxis.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final PropietarioRepository propietarioRepository;

    public VehiculoService(
            VehiculoRepository vehiculoRepository,
            PropietarioRepository propietarioRepository
    ) {
        this.vehiculoRepository = vehiculoRepository;
        this.propietarioRepository = propietarioRepository;
    }

    @Transactional(readOnly = true)
    public List<VehiculoDTO> listarTodos() {
        return vehiculoRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehiculoDTO buscarPorId(Long id) {
        Vehiculo vehiculo = buscarEntidadPorId(id);
        return convertirADTO(vehiculo);
    }

    @Transactional
    public VehiculoDTO crear(CrearVehiculoRequest request) {
        validarPatenteDuplicada(request.patente());
        validarLicenciaDuplicada(request.licencia());
        validarNroRelojFullmarDuplicado(request.nroRelojFullmar());

        Propietario propietario = buscarPropietarioPorId(request.propietarioId());

        Vehiculo vehiculo = new Vehiculo();
        asignarDatos(vehiculo, request.patente(), request.licencia(),
                request.marca(), request.modelo(), request.anio(),
                request.nroRelojFullmar(), propietario);

        return convertirADTO(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoDTO actualizar(Long id, ActualizarVehiculoRequest request) {
        Vehiculo vehiculo = buscarEntidadPorId(id);

        validarPatenteDuplicadaEnActualizacion(request.patente(), id);
        validarLicenciaDuplicadaEnActualizacion(request.licencia(), id);
        validarNroRelojFullmarDuplicadoEnActualizacion(request.nroRelojFullmar(), id);

        Propietario propietario = buscarPropietarioPorId(request.propietarioId());

        asignarDatos(vehiculo, request.patente(), request.licencia(),
                request.marca(), request.modelo(), request.anio(),
                request.nroRelojFullmar(), propietario);

        return convertirADTO(vehiculoRepository.save(vehiculo));
    }

    private Vehiculo buscarEntidadPorId(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new VehiculoNoEncontradoException(id));
    }

    private Propietario buscarPropietarioPorId(Long propietarioId) {
        return propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new PropietarioNoEncontradoException(propietarioId));
    }

    private void validarPatenteDuplicada(String patente) {
        if (vehiculoRepository.existsByPatente(patente)) {
            throw new PatenteVehiculoDuplicadaException(patente);
        }
    }

    private void validarLicenciaDuplicada(String licencia) {
        if (vehiculoRepository.existsByLicencia(licencia)) {
            throw new LicenciaVehiculoDuplicadaException(licencia);
        }
    }

    private void validarNroRelojFullmarDuplicado(String nroRelojFullmar) {
        if (vehiculoRepository.existsByNroRelojFullmar(nroRelojFullmar)) {
            throw new NroRelojFullmarDuplicadoException(nroRelojFullmar);
        }
    }

    private void validarPatenteDuplicadaEnActualizacion(String patente, Long id) {
        if (vehiculoRepository.existsByPatenteAndIdNot(patente, id)) {
            throw new PatenteVehiculoDuplicadaException(patente);
        }
    }

    private void validarLicenciaDuplicadaEnActualizacion(String licencia, Long id) {
        if (vehiculoRepository.existsByLicenciaAndIdNot(licencia, id)) {
            throw new LicenciaVehiculoDuplicadaException(licencia);
        }
    }

    private void validarNroRelojFullmarDuplicadoEnActualizacion(
            String nroRelojFullmar, Long id
    ) {
        if (vehiculoRepository.existsByNroRelojFullmarAndIdNot(nroRelojFullmar, id)) {
            throw new NroRelojFullmarDuplicadoException(nroRelojFullmar);
        }
    }

    private void asignarDatos(
            Vehiculo vehiculo,
            String patente,
            String licencia,
            String marca,
            String modelo,
            Integer anio,
            String nroRelojFullmar,
            Propietario propietario
    ) {
        vehiculo.setPatente(patente);
        vehiculo.setLicencia(licencia);
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setAnio(anio);
        vehiculo.setNroRelojFullmar(nroRelojFullmar);
        vehiculo.setPropietario(propietario);
    }

    private VehiculoDTO convertirADTO(Vehiculo vehiculo) {
        Propietario propietario = vehiculo.getPropietario();

        return VehiculoDTO.builder()
                .id(vehiculo.getId())
                .patente(vehiculo.getPatente())
                .licencia(vehiculo.getLicencia())
                .marca(vehiculo.getMarca())
                .modelo(vehiculo.getModelo())
                .anio(vehiculo.getAnio())
                .nroRelojFullmar(vehiculo.getNroRelojFullmar())
                .propietarioId(propietario != null ? propietario.getId() : null)
                .propietarioNombre(propietario != null ? propietario.getNombre() : null)
                .propietarioApellido(propietario != null ? propietario.getApellido() : null)
                .propietarioDni(propietario != null ? propietario.getDni() : null)
                .build();
    }
}