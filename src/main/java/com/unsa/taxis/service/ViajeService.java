package com.unsa.taxis.service;

import com.unsa.taxis.dto.CrearViajeRequest;
import com.unsa.taxis.dto.ViajeResponse;
import com.unsa.taxis.event.ViajeCreadoEvent;
import com.unsa.taxis.exception.TransicionEstadoViajeException;
import com.unsa.taxis.exception.ViajeNoEncontradoException;
import com.unsa.taxis.mapper.ViajeMapper;
import com.unsa.taxis.model.*;
import com.unsa.taxis.repository.ClienteRepository;
import com.unsa.taxis.repository.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unsa.taxis.exception.TokenSolicitudViajeInvalidoException;
import com.unsa.taxis.exception.AccesoNoAutorizadoException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class ViajeService {

    private final ViajeRepository viajeRepository;
    private final ClienteRepository clienteRepository;
    private final TokenSolicitudViajeService tokenSolicitudViajeService;
    private final TarifaService tarifaService;
    private final CalculadorCostoService calculadorCostoService;
    private final ViajeMapper viajeMapper;
    private final ChoferService choferService;
    private final OfertaViajeService ofertaViajeService;
    private final ApplicationEventPublisher eventPublisher;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    @Transactional
    public ViajeResponse crearViaje(CrearViajeRequest request) {

        // 1. Validar y consumir el token de solicitud
        String whatsappId = tokenSolicitudViajeService
                .consumirToken(request.getToken())
                .orElseThrow(TokenSolicitudViajeInvalidoException::new);

        // 2. Buscar al cliente por su WhatsApp
        Cliente cliente = clienteRepository
                .findByWhatsappId(whatsappId)
                .orElseGet(() -> {
                    Cliente nuevoCliente = Cliente.builder()
                            .whatsappId(whatsappId)
                            .nombre(request.getNombreCliente())
                            .build();

                    return clienteRepository.save(nuevoCliente);
                });

        // 3. Obtener fecha y hora actual
        OffsetDateTime ahora = OffsetDateTime.now(
                ZoneId.of("America/Argentina/Salta")
        );

        // 4. Determinar la tarifa correspondiente
        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(ahora);

        // 5. Calcular el costo estimado
        BigDecimal costoEstimado = calculadorCostoService.calcularCosto(
                request.getLatitudOrigen(),
                request.getLongitudOrigen(),
                request.getLatitudDestino(),
                request.getLongitudDestino(),
                tarifa
        );

        // 6. Crear el viaje
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
                .rutaGeometria(request.getRutaGeometria())
                .distanciaMetros(request.getDistanciaMetros())
                .duracionSegundos(request.getDuracionSegundos())
                .build();

        // 7. Guardar el viaje
        Viaje viajeGuardado = viajeRepository.save(viaje);

        eventPublisher.publishEvent(
                new ViajeCreadoEvent(viajeGuardado.getId())
        );

        //8. Retornar el dto
        return viajeMapper.toResponse(viajeGuardado);
    }

    public ViajeResponse buscarPorId(Long id, Authentication authentication) {

        boolean esAdmin = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        Chofer choferAutenticado = null;

        if (!esAdmin) {
                choferAutenticado = usuarioAutenticadoService.obtenerChofer(authentication);
        }

        Viaje viaje = viajeRepository.findById(id)
                .orElseThrow(() -> new ViajeNoEncontradoException(id));

        if (!esAdmin && (
                viaje.getChofer() == null
                || !viaje.getChofer().getId().equals(choferAutenticado.getId())
        )) {
                throw new AccesoNoAutorizadoException(
                        "No tenés autorización para consultar este viaje"
                );
        }

        return viajeMapper.toResponse(viaje);
    }

    public Viaje buscarEntidadPorId(Long id) {

        return viajeRepository.findById(id)
                .orElseThrow(() -> new ViajeNoEncontradoException(id));
    }

    public void iniciarViaje(Long viajeId, Authentication authentication) {

        Viaje viaje = buscarEntidadPorId(viajeId);

        if (viaje.getEstado() != EstadoViaje.ACEPTADO) {
            throw new TransicionEstadoViajeException(
                    "No se puede iniciar el viaje porque no está aceptado"
            );
        }

        validarChoferAsignado(viaje, authentication);

        viaje.setEstado(EstadoViaje.EN_CURSO);

        viajeRepository.save(viaje);
    }

    public void finalizarViaje(Long viajeId, Authentication authentication) {
        Viaje viaje = buscarEntidadPorId(viajeId);

        if (viaje.getEstado() != EstadoViaje.EN_CURSO) {
            throw new TransicionEstadoViajeException(
                    "No se puede finalizar el viaje porque no está en curso"
            );
        }

        validarChoferAsignado(viaje, authentication);

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

    @Transactional
    public void cancelarViajePorWhatsapp(String whatsappId) {

        List<EstadoViaje> estadosActivos = List.of(
                EstadoViaje.SOLICITADO,
                EstadoViaje.ACEPTADO,
                EstadoViaje.EN_CURSO
        );

        List<Viaje> viajesActivos =
                viajeRepository.findByCliente_WhatsappIdAndEstadoInOrderByFechaCreacionDesc(
                        whatsappId,
                        estadosActivos
                );

        if (viajesActivos.isEmpty()) {
                throw new TransicionEstadoViajeException(
                         "No tenés viajes activos para cancelar."
                );
        }

        Long viajeId = viajesActivos.get(0).getId();

        Viaje viaje = viajeRepository.buscarPorIdConBloqueo(viajeId)
                .orElseThrow(() -> new ViajeNoEncontradoException(viajeId));

        // Verificamos nuevamente que el viaje pertenezca al remitente.
        if (!viaje.getCliente().getWhatsappId().equals(whatsappId)) {
                throw new TransicionEstadoViajeException(
                        "No tenés viajes activos para cancelar."
                );
        }

        if (viaje.getEstado() != EstadoViaje.SOLICITADO) {
                throw new TransicionEstadoViajeException(
                        "No se puede cancelar el viaje porque ya fue aceptado o está en curso."
                );
        }

        viaje.setEstado(EstadoViaje.CANCELADO);

        ofertaViajeService.cancelarTodasLasOfertasPendientes(viaje);

        viajeRepository.save(viaje);
     }

     private void validarChoferAsignado(
                Viaje viaje,
                Authentication authentication) {

        Chofer choferAutenticado =
                usuarioAutenticadoService.obtenerChofer(authentication);

        if (viaje.getChofer() == null
                || !viaje.getChofer().getId().equals(choferAutenticado.getId())) {

            throw new AccesoNoAutorizadoException(
                        "No tenés autorización para gestionar este viaje"
            );
        }
     }
}