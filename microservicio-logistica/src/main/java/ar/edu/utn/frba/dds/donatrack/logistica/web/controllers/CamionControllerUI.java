package ar.edu.utn.frba.dds.donatrack.logistica.web.controllers;

import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Camion;
import ar.edu.utn.frba.dds.donatrack.logistica.dominio.camion.Gps;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.CamionRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.GpsRepository;
import ar.edu.utn.frba.dds.donatrack.logistica.persistencia.WithLogisticaPersistenceUnit;
import ar.edu.utn.frba.dds.donatrack.logistica.web.convers.CamionMapper;
import ar.edu.utn.frba.dds.donatrack.logistica.web.dto.camion.ActualizarCamionRequest;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CamionControllerUI implements WithLogisticaPersistenceUnit {
  private CamionRepository repoCamiones;
  private GpsRepository repoGps;

  public CamionControllerUI(CamionRepository camionRepository, GpsRepository gpsRepository) {
    this.repoCamiones = camionRepository;
    this.repoGps = gpsRepository;
  }

  public void mostrarTodos(Context context) {
    List<Camion> camiones = repoCamiones.buscarTodos();
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("camiones", CamionMapper.aDto(camiones));
    context.render("camion.hbs", modelo);
  }

  public void mostrarFormularioCreacion(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    context.render("camion_formulario_creacion.hbs", modelo);
  }

  public void guardar(Context context) {
    String patente = context.formParam("patente");
    Float volumen = Float.parseFloat(context.formParam("capacidad_volumen"));
    Float altura = Float.parseFloat(context.formParam("altura"));
    Float carga = Float.parseFloat(context.formParam("capacidad_carga"));

    Camion nuevoCamion = new Camion(patente, volumen, altura, carga);

    beginTransaction();
    repoCamiones.guardar(nuevoCamion);
    commitTransaction();

    context.redirect("/camiones");
  }

  public void mostrarFormularioEdicion(Context context) {
    String patente = context.pathParam("patente");
    Camion camion = repoCamiones.buscarPorPatente(patente);

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("camion", CamionMapper.aDto(camion));

    context.render("camion_formulario_edicion.hbs", modelo);
  }

  public void actualizar(Context context) {
    String patente = context.pathParam("patente");
    Camion camion = repoCamiones.buscarPorPatente(patente);

    Float volumen = Float.parseFloat(context.formParam("capacidad_volumen"));
    Float altura = Float.parseFloat(context.formParam("altura"));
    Float carga = Float.parseFloat(context.formParam("capacidad_carga"));
    String gpsImei = context.formParam("gps_imei");

    ActualizarCamionRequest request = new ActualizarCamionRequest(volumen, altura, carga, gpsImei);

    Gps nuevoGps = null;
    if (gpsImei != null && !gpsImei.isBlank()) {
      nuevoGps = repoGps.buscarPorId(gpsImei);
    }

    beginTransaction();
    CamionMapper.actualizarDesdeRequest(camion, nuevoGps, request);
    repoCamiones.actualizar(camion);
    entityManager().flush();
    entityManager().clear();
    commitTransaction();

    context.redirect("/camiones");
  }

  public void eliminar(Context context) {
    String patente = context.pathParam("patente");
    //Camion camion = repoCamiones.buscarPorPatente(patente);

    beginTransaction();
    repoCamiones.eliminar(patente);
    commitTransaction();

    context.redirect("/camiones");
  }

}
