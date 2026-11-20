package ar.edu.utn.frba.dds.donatrack.logistica.persistencia;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.DonacionEnTransito;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Coordenada;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Gps;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Chofer;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.ruta.Ruta;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.LocalDate;
import java.util.List;

public class DatosIniciales implements WithLogisticaPersistenceUnit {
  private final CamionRepository repoCamiones = CamionRepository.getInstancia();
  private final EntregaRepository repoEntregas = EntregaRepository.getInstancia();
  private final BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
  private final GpsRepository repogps = GpsRepository.getInstancia();
  private final RutaRepository repoRutas = RutaRepository.getInstancia();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    if (!repoCamiones.buscarTodos().isEmpty()) {
      return;
    }

    // 1. Camiones y GPS distribuidos por CABA
    Camion camion1 = new Camion("ABC-123", 10f, 2.5f, 1500f);
    Gps gps1 = new Gps("000A");
    camion1.agregarGps(gps1);
    camion1.agregarCoordenada(new Coordenada("-34.6315", "-58.4832")); // Floresta -> Villa Luro

    Camion camion2 = new Camion("DEF-456", 15f, 2.8f, 2000f);
    Gps gps2 = new Gps("000B");
    camion2.agregarGps(gps2);
    camion2.agregarCoordenada(new Coordenada("-34.5985", "-58.4380")); // Villa Crespo -> Caballito

    Camion camion3 = new Camion("GHI-789", 12f, 2.6f, 1800f);
    Gps gps3 = new Gps("000C");
    camion3.agregarGps(gps3);
    camion3.agregarCoordenada(new Coordenada("-34.6510", "-58.5150")); // Mataderos -> Villa Luro

    Camion camion4 = new Camion("JKL-012", 14f, 2.7f, 1900f);
    Gps gps4 = new Gps("000D");
    camion4.agregarGps(gps4);
    camion4.agregarCoordenada(new Coordenada("-34.5735", "-58.4210")); // Palermo -> Barrio Norte

    // 2. Beneficiarios sincronizados con IDs 1L, 2L y 3L de Donaciones
    Beneficiario comedorAmerica = new Beneficiario(1L, "Comedor america", "Av. Rivadavia 10000, CABA");
    Beneficiario comedorPibes = new Beneficiario(2L, "Comedor Los Pibes", "Av. Rivadavia 4500, CABA");
    Beneficiario hogarSantaRita = new Beneficiario(3L, "Hogar Santa Rita", "Av. Santa Fe 3200, CABA");

    // 3. Donaciones en tránsito sincronizadas (3L, 7L, 16L..20L)
    DonacionEnTransito donacion3 = new DonacionEnTransito(3L, "Manzanas", comedorAmerica);
    DonacionEnTransito donacion16 = new DonacionEnTransito(16L, "Frazadas térmicas, Sábanas 1 plaza", comedorAmerica);
    Entrega entrega1 = new Entrega(List.of(donacion3, donacion16));
    entrega1.reasignarCamion(camion1);
    entrega1.confirmarListaParaEntregar();

    DonacionEnTransito donacion7 = new DonacionEnTransito(7L, "Arroz largo fino", comedorPibes);
    DonacionEnTransito donacion17 = new DonacionEnTransito(17L, "Leche entera larga vida, Aceite de maíz", comedorPibes);
    Entrega entrega2 = new Entrega(List.of(donacion7, donacion17));
    entrega2.reasignarCamion(camion2);
    entrega2.confirmarListaParaEntregar();

    DonacionEnTransito donacion18 = new DonacionEnTransito(18L, "Camperas impermeables", comedorAmerica);
    Entrega entrega3 = new Entrega(List.of(donacion18));
    entrega3.reasignarCamion(camion3);
    entrega3.confirmarListaParaEntregar();

    DonacionEnTransito donacion19 = new DonacionEnTransito(19L, "Pañales infantiles", hogarSantaRita);
    DonacionEnTransito donacion20 = new DonacionEnTransito(20L, "Lavandina concentrada, Detergente biodegradable", hogarSantaRita);
    Entrega entrega4 = new Entrega(List.of(donacion19, donacion20));
    entrega4.reasignarCamion(camion4);
    entrega4.confirmarListaParaEntregar();

    // 4. Choferes y Rutas iniciadas
    Chofer chofer1 = new Chofer("LIC-111", "Carlos", "Gómez");
    Chofer chofer2 = new Chofer("LIC-222", "Marta", "López");
    Chofer chofer3 = new Chofer("LIC-333", "Diego", "Fernández");
    Chofer chofer4 = new Chofer("LIC-444", "Laura", "Sánchez");

    Ruta ruta1 = new Ruta(camion1, LocalDate.now(), List.of(entrega1));
    ruta1.asignarChofer(chofer1);
    ruta1.iniciarRecorrido();

    Ruta ruta2 = new Ruta(camion2, LocalDate.now(), List.of(entrega2));
    ruta2.asignarChofer(chofer2);
    ruta2.iniciarRecorrido();

    Ruta ruta3 = new Ruta(camion3, LocalDate.now(), List.of(entrega3));
    ruta3.asignarChofer(chofer3);
    ruta3.iniciarRecorrido();

    Ruta ruta4 = new Ruta(camion4, LocalDate.now(), List.of(entrega4));
    ruta4.asignarChofer(chofer4);
    ruta4.iniciarRecorrido();

    try {
      beginTransaction();
      repogps.guardar(gps1);
      repogps.guardar(gps2);
      repogps.guardar(gps3);
      repogps.guardar(gps4);

      repoCamiones.guardar(camion1);
      repoCamiones.guardar(camion2);
      repoCamiones.guardar(camion3);
      repoCamiones.guardar(camion4);

      repoBeneficiarios.guardar(comedorAmerica);
      repoBeneficiarios.guardar(comedorPibes);
      repoBeneficiarios.guardar(hogarSantaRita);

      repoEntregas.guardar(entrega1);
      repoEntregas.guardar(entrega2);
      repoEntregas.guardar(entrega3);
      repoEntregas.guardar(entrega4);

      repoRutas.guardar(ruta1);
      repoRutas.guardar(ruta2);
      repoRutas.guardar(ruta3);
      repoRutas.guardar(ruta4);

      commitTransaction();
      System.out.println("Datos iniciales de Logística cargados y sincronizados.");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar los datos iniciales de Logística.");
      e.printStackTrace();
    } finally {
      WithLogisticaPersistenceUnit.dispose();
    }
  }
}