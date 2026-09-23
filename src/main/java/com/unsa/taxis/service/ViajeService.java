package com.unsa.taxis.service;

import com.unsa.taxis.dto.CrearViajeRequest;
import com.unsa.taxis.model.Cliente;
import com.unsa.taxis.model.Tarifa;
import com.unsa.taxis.model.Viaje;
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
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));

        return viajeMapper.toResponse(viaje);
    }

    public Viaje buscarEntidadPorId(Long id) {

        return viajeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Viaje no encontrado"));
    }

    public List<ViajeResponse> listarTodos() {
        return viajeRepository.findAll()
                .stream()
                .map(viajeMapper::toResponse)
                .toList();
    }
}