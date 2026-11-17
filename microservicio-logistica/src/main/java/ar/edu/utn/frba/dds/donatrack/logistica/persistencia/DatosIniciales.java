package ar.edu.utn.frba.dds.donatrack.logistica.persistencia;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.beneficiario.DonacionEnTransito;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Gps;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.entrega.Entrega;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.util.List;

public class DatosIniciales implements WithLogisticaPersistenceUnit {
  private final CamionRepository repoCamiones = CamionRepository.getInstancia();
  private final EntregaRepository repoEntregas = EntregaRepository.getInstancia();
  private final BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
  private final GpsRepository repogps = GpsRepository.getInstancia();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    //Entidades
    Camion camion = new Camion("ABC-123", 10f, 2.5f, 1500f);
    Gps gps = new Gps("000A");
    Beneficiario beneficiario = new Beneficiario(5L, "Comedor E", "Calle 5");
    DonacionEnTransito donacion = new DonacionEnTransito(5L, "Fideos", beneficiario);
    Entrega entrega = new Entrega(List.of(donacion));

    //Acciones
    camion.agregarGps(gps);
    entrega.reasignarCamion(camion);
    entrega.confirmarListaParaEntregar();
    entrega.iniciarTraslado();

    //Transacción
    try {
      beginTransaction();
      repogps.guardar(gps);
      repoCamiones.guardar(camion);
      repoBeneficiarios.guardar(beneficiario);
      repoEntregas.guardar(entrega);
      commitTransaction();
      System.out.println("Datos de prueba del donante cargados (donante / donante).");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar los datos de prueba del donante.");
      e.printStackTrace();
    }
  }

}