package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.ActualizarNecesidadRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.DatosEdicionNecesidadResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.DatosFormularioNecesidadResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.GuardarNecesidadRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListarNecesidadesRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ListarNecesidadesResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.NecesidadIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class NecesidadController {

  private final DonacionesServiceBlockingStub donacionesStub;

  public NecesidadController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void listar(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario() && !usuarioActual.isAdmin()) {
      context.status(403).result("No tenés permiso para consultar necesidades");
      return;
    }

    // Si es beneficiario (no admin), verificamos que tenga un idBeneficiario vinculado
    if (!usuarioActual.isAdmin() && usuarioActual.getIdBeneficiario() == null) {
      context.status(404).result("No existe un beneficiario asociado a este usuario");
      return;
    }

    try {
      ListarNecesidadesRequest request = ListarNecesidadesRequest.newBuilder()
          .setEsAdmin(usuarioActual.isAdmin())
          .setIdBeneficiario(usuarioActual.getIdBeneficiario() != null ? usuarioActual.getIdBeneficiario() : 0L)
          .build();

      ListarNecesidadesResponse response = donacionesStub.listarNecesidades(request);

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", usuarioActual);
      modelo.put("esAdmin", usuarioActual.isAdmin());
      modelo.put("necesidades", response.getNecesidadesList());

      context.render("donaciones/views/beneficiario/necesidades.hbs", modelo);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        context.status(404).result(e.getStatus().getDescription());
      } else {
        throw e;
      }
    }
  }

  public void mostrarFormulario(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isBeneficiario() && !usuarioActual.isAdmin()) {
      context.status(403).result("No tenés permiso para registrar necesidades");
      return;
    }

    DatosFormularioNecesidadResponse response = donacionesStub.obtenerDatosFormularioNecesidad(
        Vacio.getDefaultInstance()
    );

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("categorias", response.getCategoriasList());
    modelo.put("subcategorias", response.getSubcategoriasList());

    context.render("donaciones/views/beneficiario/nueva_necesidad.hbs", modelo);
  }

  public void mostrarFormularioEdicion(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isAdmin()) {
      context.status(403).result("No tenés permiso para editar necesidades");
      return;
    }

    Long necesidadId = Long.valueOf(context.pathParam("id"));

    DatosEdicionNecesidadResponse response = donacionesStub.obtenerDatosEdicionNecesidad(
        NecesidadIdRequest.newBuilder().setIdNecesidad(necesidadId).build()
    );

    if (!response.getEncontrada()) {
      context.status(404).result("Necesidad no encontrada");
      return;
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("necesidad", response.getNecesidad());
    modelo.put("esExtraordinaria", response.getEsExtraordinaria());
    modelo.put("esRecurrente", response.getEsRecurrente());
    modelo.put("beneficiario", response.getBeneficiario());
    modelo.put("categorias", response.getCategoriasList());
    modelo.put("subcategorias", response.getSubcategoriasList());

    context.render("donaciones/views/beneficiario/editar_necesidad.hbs", modelo);
  }

  public void guardar(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }
    if (!usuarioActual.isBeneficiario()) {
      context.status(403).result("No tenés permiso para registrar necesidades");
      return;
    }
    if (usuarioActual.getIdBeneficiario() == null) {
      context.status(404).result("No existe un beneficiario asociado a este usuario");
      return;
    }

    GuardarNecesidadRequest request = GuardarNecesidadRequest.newBuilder()
        .setIdBeneficiario(usuarioActual.getIdBeneficiario())
        .setTipo(textoOSinValor(context.formParam("tipo")))
        .setDescripcion(textoOSinValor(context.formParam("descripcion")))
        .setUnidadMedida(textoOSinValor(context.formParam("unidadMedida")))
        .setCategoria(textoOSinValor(context.formParam("categoria")))
        .setSubcategoria(textoOSinValor(context.formParam("subcategoria")))
        .setCantidadPorPeriodo(obtenerEnteroOCero(context.formParam("cantidadPorPeriodo")))
        .setPeriodo(textoOSinValor(context.formParam("periodo")))
        .setCantidadRequerida(obtenerEnteroOCero(context.formParam("cantidadRequerida")))
        .build();

    donacionesStub.guardarNecesidad(request);

    context.redirect("/mis-necesidades");
  }

  public void actualizar(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    if (usuarioActual == null) {
      context.redirect("/login");
      return;
    }

    if (!usuarioActual.isAdmin()) {
      context.status(403).result("No tenés permiso para editar necesidades");
      return;
    }

    Long necesidadId;
    try {
      necesidadId = Long.valueOf(context.pathParam("id"));
    } catch (NumberFormatException e) {
      context.status(400).result("ID de necesidad inválido");
      return;
    }

    try {
      ActualizarNecesidadRequest request = ActualizarNecesidadRequest.newBuilder()
          .setIdNecesidad(necesidadId)
          .setDescripcion(textoOSinValor(context.formParam("descripcion")))
          .setCategoria(textoOSinValor(context.formParam("categoria")))
          .setSubcategoria(textoOSinValor(context.formParam("subcategoria")))
          .setUnidadMedida(textoOSinValor(context.formParam("unidadMedida")))
          .setCantidadPorPeriodo(obtenerEnteroOCero(context.formParam("cantidadPorPeriodo")))
          .setCantidadRequerida(obtenerEnteroOCero(context.formParam("cantidadRequerida")))
          .setPeriodo(textoOSinValor(context.formParam("periodo")))
          .build();

      donacionesStub.actualizarNecesidad(request);

      context.redirect("/mis-necesidades");
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
        context.status(404).result("Necesidad no encontrada");
      } else {
        throw e;
      }
    }
  }

  //================== FUNCIONES AUXILIARES ==================
  private int obtenerEnteroOCero(String valor) {
    if (valor == null || valor.isBlank()) {
      return 0;
    }
    return Integer.parseInt(valor);
  }

  private String textoOSinValor(String valor) {
    return valor != null ? valor : "";
  }
}