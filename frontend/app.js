

const API_BASE_URL = "https://taxis-salta-production.up.railway.app";

const parametrosUrl = new URLSearchParams(window.location.search);
const tokenSolicitud = parametrosUrl.get("token");

const formulario = document.getElementById("formulario-viaje");

const pasoOrigen = document.getElementById("paso-origen");
const pasoDestino = document.getElementById("paso-destino");
const pasoConfirmacion = document.getElementById("paso-confirmacion");

const direccionOrigen = document.getElementById("direccion-origen");
const direccionDestino = document.getElementById("direccion-destino");

const resultadosOrigen = document.getElementById("resultados-origen");
const resultadosDestino = document.getElementById("resultados-destino");

const mensaje = document.getElementById("mensaje");
const indicadores = document.querySelectorAll(".step");

const resumenOrigen = document.getElementById("resumen-origen");
const resumenDestino = document.getElementById("resumen-destino");

const resumenDistancia = document.getElementById("resumen-distancia");
const resumenDuracion = document.getElementById("resumen-duracion");

const alternativasRuta = document.getElementById("alternativas-ruta");
const listaRutas = document.getElementById("lista-rutas");

const pasos = [
    pasoOrigen,
    pasoDestino,
    pasoConfirmacion
];

function mostrarPaso(numeroPaso) {
    pasos.forEach((paso, indice) => {
        paso.hidden = indice !== numeroPaso;
    });

    indicadores.forEach((indicador, indice) => {
        if (indice === numeroPaso) {
            indicador.classList.add("active");
            indicador.setAttribute("aria-current", "step");
        } else {
            indicador.classList.remove("active");
            indicador.removeAttribute("aria-current");
        }
    });

    if (numeroPaso === 0 || numeroPaso === 1) {
        mapa.invalidateSize();
    }

    mensaje.textContent = "";

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

document.getElementById("continuar-destino")
    .addEventListener("click", () => {
        if (coordenadasOrigen === null) {
            mensaje.textContent =
                "Buscá una dirección y elegí un resultado, o seleccioná " +
                "el punto en el mapa. Si no conocés la dirección, " +
                "marcá primero el punto y después escribí una descripción.";
            return;
        }

        mostrarPaso(1);
        direccionDestino.focus();
    });

document.getElementById("volver-origen")
    .addEventListener("click", () => {
        mostrarPaso(0);
        direccionOrigen.focus();
    });




document.getElementById("continuar-confirmacion")
    .addEventListener("click", async () => {
        if (coordenadasDestino === null) {
            mensaje.textContent =
                "Buscá una dirección y elegí un resultado, o seleccioná " +
                "el punto de destino en el mapa. Después podés escribir " +
                "una descripción del lugar.";
            return;
        }

        if (coordenadasOrigen === null || coordenadasDestino === null) {
            mensaje.textContent =
                "Seleccioná un punto de origen y otro de destino en el mapa.";
            return;
        }

        const textoOrigen = direccionOrigen.value.trim();
        const textoDestino = direccionDestino.value.trim();

        resumenOrigen.textContent =
            `${textoOrigen || "Punto seleccionado en el mapa"} — ` +
            `Coordenadas: ${coordenadasOrigen.latitud.toFixed(6)}, ` +
            `${coordenadasOrigen.longitud.toFixed(6)}`;

        resumenDestino.textContent =
            `${textoDestino || "Punto seleccionado en el mapa"} — ` +
            `Coordenadas: ${coordenadasDestino.latitud.toFixed(6)}, ` +
            `${coordenadasDestino.longitud.toFixed(6)}`;

        resumenDistancia.textContent = "Calculando...";
        resumenDuracion.textContent = "Calculando...";

        mostrarPaso(2);

        await calcularRuta();
    });


document.getElementById("volver-destino")
    .addEventListener("click", () => {
        mostrarPaso(1);
        direccionDestino.focus();
    });


formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const textoOrigen = direccionOrigen.value.trim();
    const textoDestino = direccionDestino.value.trim();

    // Verificar que el enlace contenga el token de WhatsApp.
    if (!tokenSolicitud) {
        mensaje.textContent =
            "Falta el token de solicitud. Iniciá el pedido desde WhatsApp.";
        return;
    }

    // Validar las direcciones obligatorias del backend.
    if (!textoOrigen) {
        mostrarPaso(0);
        mensaje.textContent =
            "Escribí una descripción del lugar de origen.";
        direccionOrigen.focus();
        return;
    }

    if (!textoDestino) {
        mostrarPaso(1);
        mensaje.textContent =
            "Escribí una descripción del lugar de destino.";
        direccionDestino.focus();
        return;
    }

    // Verificar que ambos puntos estén seleccionados.
    if (coordenadasOrigen === null) {
        mostrarPaso(0);
        mensaje.textContent =
            "Seleccioná el punto de origen en el mapa.";
        return;
    }

    if (coordenadasDestino === null) {
        mostrarPaso(1);
        mensaje.textContent =
            "Seleccioná el punto de destino en el mapa.";
        return;
    }

    const rutaSeleccionada = rutasDisponibles[indiceRutaSeleccionada];

    const solicitud = {
        token: tokenSolicitud,
        nombreCliente: null,
        direccionOrigen: textoOrigen,
        latitudOrigen: coordenadasOrigen.latitud,
        longitudOrigen: coordenadasOrigen.longitud,
        direccionDestino: textoDestino,
        latitudDestino: coordenadasDestino.latitud,
        longitudDestino: coordenadasDestino.longitud,
        rutaGeometria: rutaSeleccionada
            ? JSON.stringify(rutaSeleccionada.geometry)
            : null,
        distanciaMetros: rutaSeleccionada
            ? Math.round(rutaSeleccionada.distance)
            : null,
        duracionSegundos: rutaSeleccionada
            ? Math.round(rutaSeleccionada.duration)
            : null
    };

    const botonSolicitar =
        document.getElementById("solicitar-viaje");

    botonSolicitar.disabled = true;
    mensaje.textContent = "Enviando tu solicitud de taxi...";

    try {
        const respuesta = await fetch(
            `${API_BASE_URL}/api/viajes`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(solicitud)
            }
        );

        const contenido = await respuesta.text();
        let resultado = null;

        if (contenido) {
            try {
                resultado = JSON.parse(contenido);
            } catch {
                resultado = null;
            }
        }

        if (!respuesta.ok) {
            if (respuesta.status === 400) {
                throw new Error(
                    resultado?.message ||
                    "Los datos enviados no son válidos. Revisá el origen y el destino."
                );
            }

            if (respuesta.status === 401 || respuesta.status === 403) {
                throw new Error(
                    "No se pudo autorizar la solicitud. Volvé a iniciar el pedido desde WhatsApp."
                );
            }

            if (respuesta.status === 409) {
                throw new Error(
                    "El token ya fue utilizado o la solicitud entra en conflicto. Iniciá un nuevo pedido desde WhatsApp."
                );
            }

            throw new Error(
                resultado?.message ||
                `No se pudo solicitar el taxi (HTTP ${respuesta.status}).`
            );
        }

        mensaje.textContent =
            "¡Solicitud enviada correctamente! Tu pedido fue registrado.";

        botonSolicitar.textContent = "Solicitud enviada";
    } catch (error) {
        console.error("Error al solicitar el viaje:", error);

        if (error instanceof TypeError) {
            mensaje.textContent =
                "No pudimos comunicarnos con el servidor. " +
                "Verificá tu conexión. Si el problema continúa, " +
                "podría ser un error de configuración CORS.";
        } else {
            mensaje.textContent =
                error.message ||
                "Ocurrió un error al enviar la solicitud.";
        }
    } finally {
        botonSolicitar.disabled = false;
    }
});



document.getElementById("usar-ubicacion")
    .addEventListener("click", () => {
        if (!navigator.geolocation) {
            mensaje.textContent =
                "Tu navegador no permite obtener la ubicación actual.";
            return;
        }

        mensaje.textContent = "Obteniendo tu ubicación actual...";

        navigator.geolocation.getCurrentPosition(
            async (posicion) => {
                const latitud = posicion.coords.latitude;
                const longitud = posicion.coords.longitude;
                const ubicacion = [latitud, longitud];

                coordenadasOrigen = { latitud, longitud };

                if (marcadorOrigen === null) {
                    marcadorOrigen = L.marker(ubicacion).addTo(mapa);
                } else {
                    marcadorOrigen.setLatLng(ubicacion);
                }

                mapa.setView(ubicacion, 17);

                mensaje.textContent =
                    "Ubicación obtenida. Buscando la dirección...";

                try {
                    const parametros = new URLSearchParams({
                        format: "jsonv2",
                        lat: latitud,
                        lon: longitud,
                        zoom: 18,
                        addressdetails: 1
                    });

                    const respuesta = await fetch(
                        "https://nominatim.openstreetmap.org/reverse?" +
                        parametros.toString()
                    );

                    if (!respuesta.ok) {
                        throw new Error(
                            "No se pudo consultar la dirección."
                        );
                    }

                    const resultado = await respuesta.json();

                    if (resultado.display_name) {
                        direccionOrigen.value =
                            resultado.display_name;

                        mensaje.textContent =
                            "Ubicación seleccionada. Revisá la dirección " +
                            "y corregila si es necesario.";
                    } else {
                        mensaje.textContent =
                            "Obtuvimos tu ubicación, pero no encontramos " +
                            "una dirección. Podés escribir una descripción " +
                            "del lugar.";
                    }
                } catch (error) {
                    console.error(
                        "Error al obtener la dirección:",
                        error
                    );

                    mensaje.textContent =
                        "Obtuvimos tu ubicación, pero no pudimos encontrar " +
                        "la dirección. Podés escribir una descripción " +
                        "del lugar.";
                }
            },
            (error) => {
                switch (error.code) {
                    case error.PERMISSION_DENIED:
                        mensaje.textContent =
                            "No autorizaste el acceso a tu ubicación. " +
                            "Podés buscar una dirección o elegir el punto en el mapa.";
                        break;

                    case error.POSITION_UNAVAILABLE:
                        mensaje.textContent =
                            "No pudimos determinar tu ubicación. " +
                            "Probá nuevamente o elegí el punto en el mapa.";
                        break;

                    case error.TIMEOUT:
                        mensaje.textContent =
                            "Se agotó el tiempo para obtener tu ubicación. " +
                            "Intentá nuevamente.";
                        break;

                    default:
                        mensaje.textContent =
                            "Ocurrió un error al obtener tu ubicación.";
                }
            },
            {
                enableHighAccuracy: true,
                timeout: 15000,
                maximumAge: 0
            }
        );
    });

let marcadorOrigen = null;
let coordenadasOrigen = null;
let marcadorDestino = null;
let coordenadasDestino = null;
let capasRutas = [];
let rutasDisponibles = [];
let indiceRutaSeleccionada = 0;


const mapa = L.map("mapa").setView([-24.7829, -65.4232], 14);



async function calcularRuta() {
    if (coordenadasOrigen === null || coordenadasDestino === null) {
        return false;
    }

    resumenDistancia.textContent = "Calculando...";
    resumenDuracion.textContent = "Calculando...";
    alternativasRuta.hidden = true;
    listaRutas.replaceChildren();

    const origen = coordenadasOrigen;
    const destino = coordenadasDestino;

    const url =
        `https://router.project-osrm.org/route/v1/driving/` +
        `${origen.longitud},${origen.latitud};` +
        `${destino.longitud},${destino.latitud}` +
        `?alternatives=3&overview=full&geometries=geojson`;

    try {
        const respuesta = await fetch(url);

        if (!respuesta.ok) {
            throw new Error("No se pudo calcular la ruta.");
        }

        const datos = await respuesta.json();

        if (
            datos.code !== "Ok" ||
            !datos.routes ||
            datos.routes.length === 0
        ) {
            throw new Error("No se encontraron rutas entre los puntos.");
        }

        // Si los pines cambiaron durante la consulta,
        // descartamos el resultado anterior.
        if (
            origen !== coordenadasOrigen ||
            destino !== coordenadasDestino
        ) {
            return false;
        }

        // Eliminar las rutas anteriores del mapa.
        capasRutas.forEach((capa) => mapa.removeLayer(capa));
        capasRutas = [];

        rutasDisponibles = datos.routes;
        indiceRutaSeleccionada = 0;

        const colores = [
            "#0f766e",
            "#7c3aed",
            "#d97706"
        ];

        rutasDisponibles.forEach((ruta, indice) => {
            const capa = L.geoJSON(ruta.geometry, {
                style: {
                    color: colores[indice % colores.length],
                    weight: indice === 0 ? 7 : 5,
                    opacity: indice === 0 ? 0.95 : 0.65
                }
            }).addTo(mapa);

            // Al pulsar una línea del mapa también se selecciona.
            capa.on("click", () => seleccionarRuta(indice));

            capasRutas.push(capa);
        });

        mapa.fitBounds(capasRutas[0].getBounds(), {
            padding: [30, 30]
        });

        mostrarAlternativasRuta();
        seleccionarRuta(0);

        return true;
    } catch (error) {
        console.error("Error al calcular la ruta:", error);

        resumenDistancia.textContent = "No disponible";
        resumenDuracion.textContent = "No disponible";

        mensaje.textContent =
            "No pudimos calcular las rutas. Podés continuar sin esa estimación.";

        return false;
    }
}


function mostrarAlternativasRuta() {
    listaRutas.replaceChildren();

    rutasDisponibles.forEach((ruta, indice) => {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.className = "opcion-ruta";
        boton.setAttribute("aria-pressed", "false");

        const titulo = document.createElement("strong");
        titulo.textContent = indice === 0
            ? "Ruta 1 — Recomendada"
            : `Ruta ${indice + 1} — Alternativa`;

        const distancia = document.createElement("span");
        distancia.textContent =
            `Distancia: ${(ruta.distance / 1000).toFixed(2)} km`;

        const minutos = Math.round(ruta.duration / 60);
        const horas = Math.floor(minutos / 60);
        const minutosRestantes = minutos % 60;

        const duracion = document.createElement("span");
        duracion.textContent =
            `Duración: ${
                horas > 0
                    ? `${horas} h ${minutosRestantes} min`
                    : `${minutos} min`
            } aprox.`;

        boton.append(titulo, distancia, duracion);
        boton.addEventListener("click", () => seleccionarRuta(indice));

        listaRutas.appendChild(boton);
    });

    alternativasRuta.hidden = rutasDisponibles.length === 0;
}

function seleccionarRuta(indice) {
    if (!rutasDisponibles[indice]) {
        return;
    }

    indiceRutaSeleccionada = indice;

    const ruta = rutasDisponibles[indice];
    const minutos = Math.round(ruta.duration / 60);
    const horas = Math.floor(minutos / 60);
    const minutosRestantes = minutos % 60;

    resumenDistancia.textContent =
        `${(ruta.distance / 1000).toFixed(2)} km`;

    resumenDuracion.textContent =
        horas > 0
            ? `${horas} h ${minutosRestantes} min aprox.`
            : `${minutos} min aprox.`;

    actualizarEstiloRutas();

    Array.from(listaRutas.children).forEach((boton, posicion) => {
        const seleccionado = posicion === indice;

        boton.classList.toggle("seleccionada", seleccionado);
        boton.setAttribute("aria-pressed", String(seleccionado));
    });

    mensaje.textContent = `Seleccionaste la ruta ${indice + 1}.`;
}

function actualizarEstiloRutas() {
    const colores = [
        "#0f766e",
        "#7c3aed",
        "#d97706"
    ];

    capasRutas.forEach((capa, indice) => {
        capa.setStyle({
            color: colores[indice % colores.length],
            weight: indice === indiceRutaSeleccionada ? 7 : 5,
            opacity: indice === indiceRutaSeleccionada ? 0.95 : 0.45
        });

        if (indice === indiceRutaSeleccionada) {
            capa.bringToFront();
        }
    });
}


L.tileLayer(
    "https://tiles.stadiamaps.com/tiles/alidade_smooth/{z}/{x}/{y}{r}.png",
    {
        maxZoom: 20,
        attribution:
            '&copy; <a href="https://stadiamaps.com/attribution/" target="_blank">Stadia Maps</a>, ' +
            '&copy; <a href="https://openmaptiles.org/" target="_blank">OpenMapTiles</a>, ' +
            '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank">OpenStreetMap</a>'
    }
).addTo(mapa);



async function buscarDireccion(tipo) {
    const campo = tipo === "origen"
        ? direccionOrigen
        : direccionDestino;

    const contenedor = tipo === "origen"
        ? resultadosOrigen
        : resultadosDestino;

    const consulta = campo.value.trim();

    contenedor.replaceChildren();
    contenedor.hidden = true;

    if (!consulta) {
        mensaje.textContent = "Escribí una dirección para buscar.";
        campo.focus();
        return;
    }

    const mostrarTexto = (texto) => {
        const parrafo = document.createElement("p");
        parrafo.className = "resultados-vacio";
        parrafo.textContent = texto;

        contenedor.replaceChildren(parrafo);
        contenedor.hidden = false;
    };

    mostrarTexto("Buscando direcciones...");
    mensaje.textContent = "";

    try {
        const busqueda = `${consulta}, Salta, Argentina`;

        const respuesta = await fetch(
            `https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&countrycodes=ar&q=${encodeURIComponent(busqueda)}`
        );

        if (!respuesta.ok) {
            throw new Error("No se pudo completar la búsqueda.");
        }

        const resultados = await respuesta.json();

        if (resultados.length === 0) {
            mostrarTexto(
                "No encontramos esa dirección. Probá con otra referencia o seleccioná el punto en el mapa."
            );
            return;
        }

        const fragmento = document.createDocumentFragment();

        resultados.forEach((resultado) => {
            const boton = document.createElement("button");

            boton.type = "button";
            boton.className = "resultado-busqueda";
            boton.textContent = resultado.display_name;

            boton.addEventListener("click", () => {
                const latitud = Number(resultado.lat);
                const longitud = Number(resultado.lon);
                const ubicacion = [latitud, longitud];

                if (tipo === "origen") {
                    coordenadasOrigen = { latitud, longitud };

                    if (marcadorOrigen === null) {
                        marcadorOrigen = L.marker(ubicacion).addTo(mapa);
                    } else {
                        marcadorOrigen.setLatLng(ubicacion);
                    }
                } else {
                    coordenadasDestino = { latitud, longitud };

                    if (marcadorDestino === null) {
                        marcadorDestino = L.marker(ubicacion).addTo(mapa);
                    } else {
                        marcadorDestino.setLatLng(ubicacion);
                    }
                }

                campo.value = resultado.display_name;

                mapa.setView(ubicacion, 17);

                contenedor.replaceChildren();
                contenedor.hidden = true;

                mensaje.textContent =
                    `Ubicación de ${tipo} seleccionada: ` +
                    `${latitud.toFixed(6)}, ${longitud.toFixed(6)}.`;
            });

            fragmento.appendChild(boton);
        });

        contenedor.replaceChildren(fragmento);
        contenedor.hidden = false;

        mensaje.textContent =
            `Encontramos ${resultados.length} resultado(s). Elegí uno de la lista.`;

    } catch (error) {
        console.error("Error al buscar la dirección:", error);

        mostrarTexto(
            "No pudimos buscar la dirección. Revisá tu conexión o seleccioná el punto en el mapa."
        );
    }
}


document.getElementById("buscar-origen")
    .addEventListener("click", () => buscarDireccion("origen"));

document.getElementById("buscar-destino")
    .addEventListener("click", () => buscarDireccion("destino"));


mapa.on("click", (evento) => {
    const latitud = evento.latlng.lat;
    const longitud = evento.latlng.lng;

    if (!pasoOrigen.hidden) {
        coordenadasOrigen = {
            latitud: latitud,
            longitud: longitud
        };

        if (marcadorOrigen === null) {
            marcadorOrigen = L.marker(evento.latlng).addTo(mapa);
        } else {
            marcadorOrigen.setLatLng(evento.latlng);
        }

        mensaje.textContent =
            `Origen seleccionado. Coordenadas: ${latitud.toFixed(6)}, ${longitud.toFixed(6)}.`;

        return;
    }

    if (!pasoDestino.hidden) {
        coordenadasDestino = {
            latitud: latitud,
            longitud: longitud
        };

        if (marcadorDestino === null) {
            marcadorDestino = L.marker(evento.latlng).addTo(mapa);
        } else {
            marcadorDestino.setLatLng(evento.latlng);
        }

        mensaje.textContent =
            `Destino seleccionado. Coordenadas: ${latitud.toFixed(6)}, ${longitud.toFixed(6)}.`;
    }
});

mostrarPaso(0);

if (!tokenSolicitud) {
    mensaje.textContent =
        "Para solicitar un taxi, iniciá la conversación desde WhatsApp y abrí el enlace que te enviamos.";
}
