
const formulario = document.getElementById("formulario-viaje");

const pasoOrigen = document.getElementById("paso-origen");
const pasoDestino = document.getElementById("paso-destino");
const pasoConfirmacion = document.getElementById("paso-confirmacion");

const direccionOrigen = document.getElementById("direccion-origen");
const direccionDestino = document.getElementById("direccion-destino");

const mensaje = document.getElementById("mensaje");
const indicadores = document.querySelectorAll(".step");

const resumenOrigen = document.getElementById("resumen-origen");
const resumenDestino = document.getElementById("resumen-destino");

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

    mensaje.textContent = "";

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}

document.getElementById("continuar-destino")
    .addEventListener("click", () => {
        if (!direccionOrigen.value.trim()) {
            mensaje.textContent =
                "Ingresá la dirección donde debemos buscarte.";
            direccionOrigen.focus();
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
    .addEventListener("click", () => {
        if (!direccionDestino.value.trim()) {
            mensaje.textContent =
                "Ingresá la dirección de destino.";
            direccionDestino.focus();
            return;
        }

        resumenOrigen.textContent = direccionOrigen.value.trim();
        resumenDestino.textContent = direccionDestino.value.trim();

        mostrarPaso(2);
    });

document.getElementById("volver-destino")
    .addEventListener("click", () => {
        mostrarPaso(1);
        direccionDestino.focus();
    });

formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();

    mensaje.textContent =
        "Los datos están preparados. Todavía falta conectar la solicitud con el backend.";
});

document.getElementById("usar-ubicacion")
    .addEventListener("click", () => {
        mensaje.textContent =
            "La ubicación actual estará disponible cuando incorporemos el mapa.";
    });

mostrarPaso(0);


const mapa = L.map("mapa").setView([-24.7829, -65.4232], 14);

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

