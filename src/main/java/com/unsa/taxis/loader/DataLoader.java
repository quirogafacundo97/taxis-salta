/*package com.unsa.taxis.loader;

import com.unsa.taxis.model.*;
import com.unsa.taxis.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    private final PropietarioRepository propietarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ChoferRepository choferRepository;
    private final ClienteRepository clienteRepository;
    private final ViajeRepository viajeRepository;

    //Inyeccion de dependencias por constructor
    public DataLoader(PropietarioRepository propietarioRepository, VehiculoRepository vehiculoRepository, ChoferRepository choferRepository, ClienteRepository clienteRepository, ViajeRepository viajeRepository) {
        this.propietarioRepository = propietarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.choferRepository = choferRepository;
        this.clienteRepository = clienteRepository;
        this.viajeRepository = viajeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== INICIANDO CARGA DE DATOS DE PRUEBA DESDE JAVA ======");

        //Buscamos por DNI para saber si ya existe el propietario de prueba
        if (propietarioRepository.findByDni("40111222").isEmpty()) {
            System.out.println("Insertando nuevo Propietario, Auto y Chofer desde Java...");

            //0- CREAR Y GUARDAR PROPIETARIO
            Propietario propietarioDePrueba = Propietario.builder()
                    .nombre("Luis")
                    .apellido("Rodriguez")
                    .dni("40111222")
                    .telefono("+5493875888888")
                    .build();

            propietarioDePrueba = propietarioRepository.save(propietarioDePrueba);
            System.out.println("Propietario guardado: " + propietarioDePrueba.getNombre() + " " + propietarioDePrueba.getApellido() + " (ID: " + propietarioDePrueba.getId() + ")");

            //1- CREAR UN NUEVO VEHICULO
            //Usando el patron @Builder de Lombok para crear el objeto de manera limpia
            Vehiculo autoDePrueba = Vehiculo.builder()
                    .patente("AA999OP")
                    .modelo("Fiat Cronos 2023")
                    .nroRelojFullmar("FM-99512-D")
                    .licencia("864")
                    .propietario(propietarioDePrueba)
                    .build();

            autoDePrueba = vehiculoRepository.save(autoDePrueba);
            System.out.println("Vehiculo guardado: " + autoDePrueba.getModelo());

            //2- CREAR UN CHOFER NUEVO ASOCIADO AL AUTO
            Chofer choferDePrueba = Chofer.builder()
                    .nombre("Ramon")
                    .apellido("Valdez")
                    .dni("12345678")
                    .telefonoContacto("+5493875999999")
                    .estado(EstadoChofer.LIBRE) //Listo para recibir viajes
                    .zonaActual("CENTRO")
                    .vehiculo(autoDePrueba) // Relaciono el chofer con el auto creado
                    .build();
            choferDePrueba = choferRepository.save(choferDePrueba);
            System.out.println("Chofer guardado: " + choferDePrueba.getNombre() + choferDePrueba.getApellido());

            //3- CREAR UN CLIENTE NUEVO (Pasajero que simula usar el bot de whatsapp)
            Cliente clienteDePrueba = Cliente.builder()
                    .whatsappId("5493875111222")
                    .nombre("Maria Becerra")
                    .build();

            clienteDePrueba = clienteRepository.save(clienteDePrueba);
            System.out.println("Cliente guardado: " + clienteDePrueba.getNombre());

            //4- CREAR UN VIAJE NUEVO (Simulando que Maria solicita un traslado)
            Viaje viajeDePrueba = Viaje.builder()
                    .cliente(clienteDePrueba)
                    .chofer(choferDePrueba)
                    .direccionOrigen("Plaza 9 Julio, Salta")
                    .direccionDestino("Terminal de Ómnibus de Salta")
                    .zonaOrigen("CENTRO")
                    .estado(EstadoViaje.SOLICITADO)
                    .tipoTarifa(TipoTarifa.DIURNA)
                    .tarifaReloj(new BigDecimal("1500.00"))
                    .build();
            viajeDePrueba = viajeRepository.save(viajeDePrueba);
            System.out.println("Viaje guardado: De " + viajeDePrueba.getDireccionOrigen() + " a " + viajeDePrueba.getDireccionDestino());

            System.out.println("====== CARGA DE DATOS FINALIZADA CON ÉXITO ======");
        }else{
            System.out.println("Ya existe el propietario");
        }

        // TRAER DATOS
        // Esto va a leer tanto lo de DBeaver (Juan, Carlos, Mario) como lo de Java (Ramón)
        List<Chofer> todosLosChoferes = choferRepository.findAll();

        System.out.println("\n¡CONEXIÓN Y MAPEOS EXITOSOS! Lista completa de Choferes Activos:");
        for (Chofer chofer : todosLosChoferes) {
            System.out.println("----------------------------------------");
            System.out.println("Chofer: " + chofer.getNombre() + " " + chofer.getApellido() + " (DNI: " + chofer.getDni() + ")");

            if (chofer.getVehiculo() != null) {
                System.out.println("Auto: " + chofer.getVehiculo().getModelo() + " | Patente: " + chofer.getVehiculo().getPatente());
                System.out.println("Licencia AMT: " + chofer.getVehiculo().getLicencia());

                if (chofer.getVehiculo().getPropietario() != null) {
                    System.out.println("Propietario: " + chofer.getVehiculo().getPropietario().getNombre() + " " + chofer.getVehiculo().getPropietario().getApellido());
                }
            } else {
                System.out.println("Sin vehículo asignado.");
            }
        }
        System.out.println("----------------------------------------");
        System.out.println("====== VERIFICACIÓN INTEGRAL FINALIZADA CON ÉXITO ======");

    }


}
*/