package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DonacionBeneficiarioController implements WithSimplePersistenceUnit {

  private final BeneficiarioRepository beneficiarioRepository;
  private final DonacionRepository donacionRepository;

  public DonacionBeneficiarioController(
      BeneficiarioRepository beneficiarioRepository, DonacionRepository donacionRepository) {
    this.beneficiarioRepository = beneficiarioRepository;
    this.donacionRepository = donacionRepository;
  }

  public void listar(Context context) {

    beginTransaction();

    try {
      Beneficiario beneficiario = beneficiarioRepository.buscarTodos()
          .stream()
          .findFirst()
          .orElse(null);
      System.out.println("Beneficiaria: " + beneficiario.getRazonSocial());
      System.out.println("Cantidad de donaciones: " + beneficiario.getDonaciones().size());

      Map<String, Object> modelo = new HashMap<>();

      if (beneficiario != null) {
        modelo.put("donaciones", beneficiario.getDonaciones());
      }

      commitTransaction();

      context.render("mis_donaciones.hbs", modelo);

    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }

  }

  public void verDetalle(Context context) {
    beginTransaction();

    try {
      Long idDonacion = Long.valueOf(context.pathParam("id"));

      Donacion donacion = donacionRepository.buscarPorId(idDonacion);

      if (donacion == null) {
        throw new IllegalArgumentException("No se encontró la donación");
      }

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("donacion", donacion);
      modelo.put("fechaAsignacion", donacion.getFechaAsignacion());
      /*
      DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      String fechaFormateada = donacion.getFechaAsignacion().format(formato);
      modelo.put("fechaAsignacion", fechaFormateada);
      * para mejorar el formato*/

      commitTransaction();

      context.render("detalle_donacion.hbs", modelo);

    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }
  }

}
