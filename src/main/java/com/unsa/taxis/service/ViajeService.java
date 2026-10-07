package com.unsa.taxis.service;

import com.unsa.taxis.exception.TransicionEstadoViajeException;
import com.unsa.taxis.exception.ViajeNoEncontradoException;
import com.unsa.taxis.dto.CrearViajeRequest;
import com.unsa.taxis.model.*;
import com.unsa.taxis.repository.ClienteRepository;
import com.unsa.taxis.repository.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.unsa.taxis.mapper.ViajeMapper;
import com.unsa.taxis.dto.ViajeResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViajeService {

    private final ViajeRepository viajeRepository;
    private final ClienteRepository clienteRepository;
    private final TarifaService tarifaService;
    private final CalculadorCostoService calculadorCostoService;
    private final ViajeMapper viajeMapper;
    private final ChoferService choferService;

    public ViajeResponse crearViaje(CrearViajeRequest request) {

        // 1. Buscar al cliente por su WhatsApp
        Cliente cliente = clienteRepository
                .findByWhatsappId(request.getWhatsappId())
                .orElseGet(() -> {
                    Cliente nuevoCliente = Cliente.builder()
                            .whatsappId(request.getWhatsappId())
                            .nombre(request.getNombreCliente())
                            .build();

                    return clienteRepository.save(nuevoCliente);
                });

        // 2. Obtener fecha y hora actual
        OffsetDateTime ahora = OffsetDateTime.now();

        // 3. Determinar la tarifa correspondiente
        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(ahora);

        // 4. Calcular el costo estimado
        BigDecimal costoEstimado = calculadorCostoService.calcularCosto(
                request.getLatitudOrigen(),
                request.getLongitudOrigen(),
                request.getLatitudDestino(),
                request.getLongitudDestino(),
                tarifa
        );

        // 5. Crear el viaje
        Viaje viaje = Viaje.builder()
                .cliente(cliente)
                .direccionOrigen(request.getDireccionOrigen())
                .latitudOrigen(request.getLatitudOrigen())
                .longitudOrigen(request.getLongitudOrigen())
                .direccionDestino(request.getDireccionDestino())
                .latitudDestino(request.getLatitudDestino())
                .longitudDestino(request.getLongitudDestino())
                .tipoTarifa(tarifa.getTipo())
                .costoEstimado(costoEstimado)
                .build();

        // 6. Guardar el viaje
        Viaje viajeGuardado = viajeRepository.save(viaje);

        //7. Retornar el dto
        return viajeMapper.toResponse(viajeGuardado);
    }

    public ViajeResponse buscarPorId(Long id) {

        Viaje viaje = viajeRepository.findById(id)
                .orElseThrow(() -> new ViajeNoEncontradoException(id));

        return viajeMapper.toResponse(viaje);
    }

    public Viaje buscarEntidadPorId(Long id) {

        return viajeRepository.findById(id)
                .orElseThrow(() -> new ViajeNoEncontradoException(id));
    }

    public void iniciarViaje(Long viajeId) {

        Viaje viaje = buscarEntidadPorId(viajeId);

        if (viaje.getEstado() != EstadoViaje.ACEPTADO) {
            throw new TransicionEstadoViajeException(
                    "No se puede iniciar el viaje porque no está aceptado"
            );
        }

        viaje.setEstado(EstadoViaje.EN_CURSO);

        viajeRepository.save(viaje);
    }

    public void finalizarViaje(Long viajeId) {
        Viaje viaje = buscarEntidadPorId(viajeId);

        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new TransicionEstadoViajeException(
                    "No se puede finalizar el viaje porque no está en curso"
            );
        }

        viaje.setEstado(EstadoViaje.FINALIZADO);

        choferService.cambiarEstado(
                viaje.getChofer().getId(),
                EstadoChofer.LIBRE
        );

        viajeRepository.save(viaje);
    }

    public List<ViajeResponse> listarTodos() {
        return viajeRepository.findAll()
                .stream()
                .map(viajeMapper::toResponse)
                .toList();
    }

    public void cancelarViaje(Long viajeId) {

        Viaje viaje = buscarEntidadPorId(viajeId);

        if (viaje.getEstado() != EstadoViaje.SOLICITADO) {

            String motivo = switch (viaje.getEstado()) {
                case ACEPTADO ->
                        "el viaje ya fue aceptado por un chofer";
                case EN_CURSO ->
                        "el viaje ya está en curso";
                case FINALIZADO ->
                        "el viaje ya finalizó";
                case CANCELADO ->
                        "el viaje ya fue cancelado";
                default ->
                        "el estado actual del viaje no permite cancelarlo";
            };

            throw new TransicionEstadoViajeException(
                    "No se puede cancelar el viaje porque " + motivo
            );
        }

        viaje.setEstado(EstadoViaje.CANCELADO);

        viajeRepository.save(viaje);
    }
}