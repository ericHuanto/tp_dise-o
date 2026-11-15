package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.EstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonacionBeneficiarioController implements WithSimplePersistenceUnit {

  private final BeneficiarioRepository beneficiarioRepository;
  private final DonacionRepository donacionRepository;

  public DonacionBeneficiarioController(
      BeneficiarioRepository beneficiarioRepository, DonacionRepository donacionRepository) {
    this.beneficiarioRepository = beneficiarioRepository;
    this.donacionRepository = donacionRepository;
  }



  public void listar(Context context) {

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario()) {
      context.status(403)
          .result("No tenés permiso para consultar donaciones");
      return;
    }

    beginTransaction();

    try {
      Beneficiario beneficiario =
          beneficiarioRepository.buscarPorUsuarioId(
              usuarioActual.getId()
          );

      if (beneficiario == null) {
        rollbackTransaction();
        context.status(404)
            .result("No existe un beneficiario asociado a este usuario");
        return;
      }

      Map<String, Object> modelo = new HashMap<>();

      modelo.put("usuario", usuarioActual);
      modelo.put("donaciones", beneficiario.getDonaciones());

      // Cargar la colección dentro de la transacción
      beneficiario.getDonaciones().size();

      commitTransaction();

      context.render("views/donante/mis_donaciones.hbs", modelo);

    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }
  }



  public void verDetalle(Context context) {

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario()) {
      context.status(403)
          .result("No tenés permiso para consultar donaciones");
      return;
    }

    Long idDonacion;

    try {
      idDonacion = Long.valueOf(context.pathParam("id"));
    } catch (NumberFormatException e) {
      context.status(400).result("ID de donación inválido");
      return;
    }

    beginTransaction();

    try {
      Beneficiario beneficiario =
          beneficiarioRepository.buscarPorUsuarioId(usuarioActual.getId());

      if (beneficiario == null) {
        rollbackTransaction();
        context.status(404)
            .result("Beneficiario no encontrado");
        return;
      }

      Donacion donacion = beneficiario.getDonaciones()
          .stream()
          .filter(d -> idDonacion.equals(d.getId()))
          .findFirst()
          .orElse(null);

      if (donacion == null) {
        rollbackTransaction();
        context.status(404)
            .result("Donación no encontrada para este beneficiario");
        return;
      }

      Map<String, Object> modelo = new HashMap<>();

      modelo.put("usuario", usuarioActual);
      modelo.put("donacion", donacion);
      modelo.put("fechaAsignacion", donacion.getFechaAsignacion());

      commitTransaction();

      context.render("views/donante/detalle_donacion.hbs", modelo);

    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }
  }

  public void mostrarNovedades(Context context) {

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario()) {
      context.status(403)
          .result("No tenés permiso para consultar notificaciones");
      return;
    }

    beginTransaction();

    try {
      Beneficiario beneficiario =
          beneficiarioRepository.buscarPorUsuarioId(usuarioActual.getId());

      if (beneficiario == null) {
        rollbackTransaction();
        context.status(404)
            .result("Beneficiario no encontrado");
        return;
      }

      List<Donacion> donaciones =
          donacionRepository.buscarPorBeneficiario(beneficiario.getId());

      List<Map<String, Object>> novedades = new ArrayList<>();

      for (Donacion donacion : donaciones) {

        for (EstadoDonacion estado : donacion.getHistorialEstados()) {

          if (estado.getTipoEstado() == TipoEstadoDonacion.EN_DEPOSITO) {
            continue;
          }

          Map<String, Object> novedad = new HashMap<>();

          novedad.put("idDonacion", donacion.getId());
          novedad.put("descripcion", donacion.getDescripcion());
          novedad.put("tipoEstado", estado.getTipoEstado().name());
          novedad.put("fecha", estado.getFecha());
          novedad.put("detalle", estado.getDetalle());

          novedades.add(novedad);
        }
      }
      novedades.sort(
          Comparator.comparing(
              n -> (java.time.LocalDateTime) n.get("fecha"),
              Comparator.nullsLast(Comparator.reverseOrder())
          )
      );

      Map<String, Object> modelo = new HashMap<>();

      modelo.put("usuario", usuarioActual);
      modelo.put("novedades", novedades);

      commitTransaction();

      context.render("views/beneficiario/notificaciones.hbs", modelo);

    } catch (RuntimeException e) {
      rollbackTransaction();
      throw e;
    }
  }

}
