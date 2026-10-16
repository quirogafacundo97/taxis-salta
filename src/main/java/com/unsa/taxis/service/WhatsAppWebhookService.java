package com.unsa.taxis.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WhatsAppWebhookService {

    public ResultadoNotificacion procesarNotificacion(
            Map<String, Object> payload) {

        List<MensajeEntrante> mensajesEntrantes = new ArrayList<>();
        int notificacionesEstado = 0;

        if (payload == null) {
            return new ResultadoNotificacion(mensajesEntrantes, 0);
        }

        for (Object entrada : obtenerLista(payload.get("entry"))) {
            Map<String, Object> entradaMap = convertirMapa(entrada);

            for (Object cambio : obtenerLista(entradaMap.get("changes"))) {
                Map<String, Object> cambioMap = convertirMapa(cambio);
                Map<String, Object> valor = convertirMapa(
                        cambioMap.get("value")
                );

                if (valor.containsKey("statuses")) {
                    notificacionesEstado +=
                            obtenerLista(valor.get("statuses")).size();
                }

                for (Object mensaje : obtenerLista(valor.get("messages"))) {
                    Map<String, Object> mensajeMap = convertirMapa(mensaje);

                    String remitente = obtenerTexto(mensajeMap.get("from"));
                    String id = obtenerTexto(mensajeMap.get("id"));
                    String tipo = obtenerTexto(mensajeMap.get("type"));

                    String texto = null;

                    if ("text".equals(tipo)) {
                        Map<String, Object> contenidoTexto =
                                convertirMapa(mensajeMap.get("text"));

                        texto = obtenerTexto(contenidoTexto.get("body"));
                    }

                    if (remitente != null && id != null && tipo != null) {
                        mensajesEntrantes.add(
                                new MensajeEntrante(remitente, id, tipo, texto)
                        );
                    }
                }
            }
        }

        return new ResultadoNotificacion(
                List.copyOf(mensajesEntrantes),
                notificacionesEstado
        );
    }

    private List<?> obtenerLista(Object valor) {
        if (valor instanceof List<?> lista) {
            return lista;
        }
        return List.of();
    }

    private Map<String, Object> convertirMapa(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            Map<String, Object> resultado = new java.util.HashMap<>();

            mapa.forEach((clave, contenido) -> {
                if (clave instanceof String textoClave) {
                    resultado.put(textoClave, contenido);
                }
            });

            return resultado;
        }
        return Map.of();
    }

    private String obtenerTexto(Object valor) {
        return valor instanceof String texto && !texto.isBlank()
                ? texto
                : null;
    }

    public record MensajeEntrante(
            String remitente,
            String id,
            String tipo,
            String texto) {
    }

    public record ResultadoNotificacion(
            List<MensajeEntrante> mensajesEntrantes,
            int notificacionesEstado) {
    }
}