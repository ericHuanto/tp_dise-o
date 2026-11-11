package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.TipoEstadoDonacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonacionRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.DonacionMapper;
import ar.edu.utn.frba.dds.donatrack.shared.excepciones.DominioException;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonacionController implements WithSimplePersistenceUnit {
  private DonacionRepository repoDonaciones;

  public DonacionController(DonacionRepository donacionRepository) {
    this.repoDonaciones = donacionRepository;
  }

  public void mostrarDonaciones(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    String estadoQuery = context.queryParam("estado_actual");
    List<Donacion> donaciones = null;

    if (estadoQuery != null && !estadoQuery.isBlank()) {
      TipoEstadoDonacion estadoFiltro = aTipoEstadoDonacion(estadoQuery);
      donaciones = repoDonaciones.buscarPorEstadoActualCoincidenteCon(estadoFiltro);
    } else {
      donaciones = repoDonaciones.buscarTodos();
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("donaciones", DonacionMapper.aDto(donaciones));

    context.render("views/admin/donaciones_en_deposito.hbs", modelo);
  }

  public void marcarComoVencida(Context context) {
    Long idDonacion = Long.parseLong(context.pathParam("id"));
    Donacion donacion = repoDonaciones.buscarPorId(idDonacion);

    donacion.vencida();

    beginTransaction();
    repoDonaciones.actualizar(donacion);
    commitTransaction();

    context.redirect("/donaciones?estado_actual=en_deposito");
  }

  //============= FUNCIONES AUXILIARES =====================
  private TipoEstadoDonacion aTipoEstadoDonacion(String estado) {
    if (estado == null) return null;
    try {
      return TipoEstadoDonacion.valueOf(estado.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DominioException("El estado de donación: " + estado + " no existe, debe ser: " + Arrays.toString(TipoEstadoDonacion.values()));
    }
  }
}
