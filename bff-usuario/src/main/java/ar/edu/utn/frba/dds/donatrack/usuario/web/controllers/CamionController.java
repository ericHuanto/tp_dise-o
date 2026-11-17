package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.CrearCamionResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.DatosEdicionCamionResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.GuardarCamionRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListaGpsResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.LogisticaServiceGrpc.LogisticaServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.PatenteCamionRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.VacioLogistica;
import ar.edu.utn.frba.dds.donatrack.grpc.VistaCamionesResponse;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class CamionController {
  private final LogisticaServiceBlockingStub logisticaStub;

  public CamionController(LogisticaServiceBlockingStub logisticaStub) {
    this.logisticaStub = logisticaStub;
  }

  public void mostrarTodos(Context context) {
    VistaCamionesResponse response = logisticaStub.obtenerVistaCamiones(VacioLogistica.getDefaultInstance());

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("camiones", response.getCamionesList());
    modelo.put("totalCamiones", response.getTotalCamiones());
    modelo.put("camionesSinGps", response.getCamionesSinGps());
    modelo.put("camionesEnRuta", response.getCamionesEnRuta());

    context.render("logistica/views/camion.hbs", modelo);
  }

  public void mostrarFormularioCreacion(Context context) {
    ListaGpsResponse response = logisticaStub.listarGpsDisponibles(VacioLogistica.getDefaultInstance());

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("gpsDisponibles", response.getGpsDisponiblesList());

    context.render("logistica/views/camion_formulario_creacion.hbs", modelo);
  }

  public void crear(Context context) {
    try {
      GuardarCamionRequest request = armarGuardarRequest(
          context.formParam("patente"),
          context
      );

      CrearCamionResponse response = logisticaStub.crearCamion(request);

      if (response.getPatenteDuplicada()) {
        context.redirect("/camiones/nuevo?error=la_patente_ya_existe");
        return;
      }

      context.redirect("/camiones");
    } catch (Exception e) {
      e.printStackTrace();
      context.redirect("/pagina-errores");
    }
  }

  public void mostrarFormularioEdicion(Context context) {
    String patente = context.pathParam("patente");

    DatosEdicionCamionResponse response = logisticaStub.obtenerDatosEdicionCamion(
        PatenteCamionRequest.newBuilder().setPatente(patente).build()
    );

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("camion", response.getCamion());
    modelo.put("gpsDisponibles", response.getGpsDisponiblesList());

    context.render("logistica/views/camion_formulario_edicion.hbs", modelo);
  }

  public void actualizar(Context context) {
    String patente = context.pathParam("patente");
    try {
      GuardarCamionRequest request = armarGuardarRequest(patente, context);
      logisticaStub.actualizarCamion(request);
      context.redirect("/camiones");
    } catch (Exception e) {
      e.printStackTrace();
      context.redirect("/pagina-errores");
    }
  }

  public void eliminar(Context context) {
    String patente = context.pathParam("patente");
    logisticaStub.eliminarCamion(
        PatenteCamionRequest.newBuilder().setPatente(patente).build()
    );
    context.redirect("/camiones");
  }

  //====================== FUNCIONES AUXILIARES ============================
  private GuardarCamionRequest armarGuardarRequest(String patente, Context context) {
    Float volumen = Float.parseFloat(context.formParam("capacidad_volumen"));
    Float altura = Float.parseFloat(context.formParam("altura"));
    Float carga = Float.parseFloat(context.formParam("capacidad_carga"));
    String imeiGps = context.formParam("imei_gps");

    return GuardarCamionRequest.newBuilder()
        .setPatente(patente != null ? patente : "")
        .setCapacidadVolumen(volumen)
        .setAltura(altura)
        .setCapacidadCarga(carga)
        .setImeiGps(imeiGps != null ? imeiGps : "")
        .build();
  }
}