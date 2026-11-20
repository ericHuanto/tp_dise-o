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

    // 1. Camiones y GPS con ubicaciones distintas en CABA (en String)
    Camion camion1 = new Camion("ABC-123", 10f, 2.5f, 1500f);
    Gps gps1 = new Gps("000A");
    camion1.agregarGps(gps1);
    // Zona Floresta (yendo hacia Av. Rivadavia 10000)
    camion1.agregarCoordenada(new Coordenada("-34.6315", "-58.4832"));

    Camion camion2 = new Camion("DEF-456", 15f, 2.8f, 2000f);
    Gps gps2 = new Gps("000B");
    camion2.agregarGps(gps2);
    // Zona Villa Crespo / Palermo (yendo hacia Av. Rivadavia 4500)
    camion2.agregarCoordenada(new Coordenada("-34.5985", "-58.4380"));

    // 2. Beneficiarios sincronizados con IDs 1L y 2L de Donaciones
    Beneficiario comedorAmerica = new Beneficiario(1L, "Comedor america", "Av. Rivadavia 10000, CABA");
    Beneficiario comedorPibes = new Beneficiario(2L, "Comedor Los Pibes", "Av. Rivadavia 4500, CABA");

    // 3. Donaciones en tránsito sincronizadas con IDs 3L y 7L
    DonacionEnTransito donacion3 = new DonacionEnTransito(3L, "Manzanas", comedorAmerica);
    Entrega entrega1 = new Entrega(List.of(donacion3));
    entrega1.reasignarCamion(camion1);
    entrega1.confirmarListaParaEntregar();

    DonacionEnTransito donacion7 = new DonacionEnTransito(7L, "Arroz largo fino", comedorPibes);
    Entrega entrega2 = new Entrega(List.of(donacion7));
    entrega2.reasignarCamion(camion2);
    entrega2.confirmarListaParaEntregar();

    // 4. Choferes y Rutas iniciadas
    Chofer chofer1 = new Chofer("LIC-111", "Carlos", "Gómez");
    Chofer chofer2 = new Chofer("LIC-222", "Marta", "López");

    Ruta ruta1 = new Ruta(camion1, LocalDate.now(), List.of(entrega1));
    ruta1.asignarChofer(chofer1);
    ruta1.iniciarRecorrido(); // <-- Acá adentro cambia automáticamente ambas entregas a EN_TRASLADO

    Ruta ruta2 = new Ruta(camion2, LocalDate.now(), List.of(entrega2));
    ruta2.asignarChofer(chofer2);
    ruta2.iniciarRecorrido();

    // Transacción
    try {
      beginTransaction();
      repogps.guardar(gps1);
      repogps.guardar(gps2);
      repoCamiones.guardar(camion1);
      repoCamiones.guardar(camion2);
      repoBeneficiarios.guardar(comedorAmerica);
      repoBeneficiarios.guardar(comedorPibes);
      repoEntregas.guardar(entrega1);
      repoEntregas.guardar(entrega2);
      repoRutas.guardar(ruta1);
      repoRutas.guardar(ruta2);
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