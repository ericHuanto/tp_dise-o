package ar.edu.utn.frba.dds.donatrack.logistica.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Gps;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.CamionRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.GpsRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.RutaRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.WithLogisticaPersistenceUnit;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers.CamionMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.convers.GpsMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.shared.dto.camion.ActualizarCamionRequest;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CamionController implements WithLogisticaPersistenceUnit {
  private CamionRepository repoCamiones;
  private GpsRepository repoGps;
  private RutaRepository repoRutas;

  public CamionController(CamionRepository camionRepository, GpsRepository gpsRepository, RutaRepository rutaRepository) {
    this.repoCamiones = camionRepository;
    this.repoGps = gpsRepository;
    this.repoRutas = rutaRepository;
  }

  public void mostrarTodos(Context context) {
    List<Camion> camiones = repoCamiones.buscarTodos();
    long totalCamiones = camiones.size();
    long totalCamionesSinGps = camiones.stream().filter(c -> !c.tieneGps()).count();
    long totalCamionesEnRuta = repoRutas.camionesEnRutaAhora();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("camiones", CamionMapper.aDto(camiones));
    modelo.put("totalCamiones", totalCamiones);
    modelo.put("camionesSinGps", totalCamionesSinGps);
    modelo.put("camionesEnRuta", totalCamionesEnRuta);
    context.render("/vistas/camion.hbs", modelo);
  }

  public void mostrarFormularioCreacion(Context context) {
    List<Gps> listaGps = repoGps.buscarTodos();
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("gpsDisponibles", GpsMapper.aDto(listaGps));
    context.render("vistas/camion_formulario_creacion.hbs", modelo);
  }

  public void crear(Context context) {
    String imeiGps = context.formParam("imei_gps");
    Camion nuevoCamion = crearCamion(context);

    if (repoCamiones.buscarPorPatente(nuevoCamion.getPatente()) != null) {
      context.redirect("camiones/nuevo?error=la_patente_ya_existe");
      return;
    }

    beginTransaction();
    try {
      if (imeiGps != null && !imeiGps.trim().isEmpty()) {
        Gps gpsAsignado = repoGps.buscarPorId(imeiGps);
        Camion otroCamion = repoCamiones.buscarPorGps(imeiGps);
        if (otroCamion != null) {
          otroCamion.quitarGps();
          repoCamiones.actualizar(otroCamion);
        }
        nuevoCamion.agregarGps(gpsAsignado);
      } else {
        nuevoCamion.agregarGps(null);
      }
      repoCamiones.guardar(nuevoCamion);
      commitTransaction();
    } catch (Exception e) {
      rollbackTransaction();
      String mensaje = e.getMessage();
      context.redirect("pagina-errores"); //TODO implementar una pagina de errores
      return;
    }

    context.redirect("camiones");
  }

  public void mostrarFormularioEdicion(Context context) {
    String patente = context.pathParam("patente");
    Camion camion = repoCamiones.buscarPorPatente(patente);
    List<Gps> listaGps = repoGps.buscarTodos();
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("camion", CamionMapper.aDto(camion));
    modelo.put("gpsDisponibles", GpsMapper.aDto(listaGps));

    context.render("/vistas/camion_formulario_edicion.hbs", modelo);
  }

  public void actualizar(Context context) {
    String patente = context.pathParam("patente");
    Camion camion = repoCamiones.buscarPorPatente(patente);
    ActualizarCamionRequest cuasiCamion = crearCuasiCamion(context);
    String imeiGps = context.formParam("imei_gps");

    beginTransaction();
    try {
      Gps gpsAsignado = null;
      if (imeiGps != null && !imeiGps.trim().isEmpty()) {
        gpsAsignado = repoGps.buscarPorId(imeiGps);
        Camion otroCamion = repoCamiones.buscarPorGps(imeiGps);
        if (otroCamion != null && !otroCamion.getPatente().equals(camion.getPatente())) {
          otroCamion.quitarGps();
          repoCamiones.actualizar(otroCamion);
        }
      }
      CamionMapper.actualizarDesdeRequest(camion, gpsAsignado, cuasiCamion);
      repoCamiones.actualizar(camion);
      entityManager().flush();
      commitTransaction();
    } catch (Exception e) {
      rollbackTransaction();
      context.redirect("/pagina-errores"); //TODO implementar una pagina de errores
      return;
    }

    context.redirect("/camiones");
  }

  public void eliminar(Context context) {
    String patente = context.pathParam("patente");
    Camion camion = repoCamiones.buscarPorPatente(patente);

    beginTransaction();
    repoCamiones.eliminar(camion);
    commitTransaction();

    context.redirect("/camiones");
  }

  //====================== FUNCIONES AUXILIARES ============================
  private Camion crearCamion(Context context) {
    String patente = context.formParam("patente");
    Float volumen = Float.parseFloat(context.formParam("capacidad_volumen"));
    Float altura = Float.parseFloat(context.formParam("altura"));
    Float carga = Float.parseFloat(context.formParam("capacidad_carga"));

    return new Camion(patente, volumen, altura, carga);
  }

  private ActualizarCamionRequest crearCuasiCamion(Context context) {
    Float volumen = Float.parseFloat(context.formParam("capacidad_volumen"));
    Float altura = Float.parseFloat(context.formParam("altura"));
    Float carga = Float.parseFloat(context.formParam("capacidad_carga"));
    String gpsImei = context.formParam("imei_gps");

    return new ActualizarCamionRequest(volumen, altura, carga, gpsImei);
  }

}
