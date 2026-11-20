package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.LogisticaServiceGrpc.LogisticaServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.javalin.http.Context;


public class EntregaController {
  private final LogisticaServiceBlockingStub logisticaStub;
  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub;

  public EntregaController(LogisticaServiceBlockingStub logisticaStub,  DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub){
    this.logisticaStub = logisticaStub;
    this.donacionesStub = donacionesStub;
  }

  public void mostrarMisEntregas(Context context) {

    Usuario usuario = context.sessionAttribute("usuario_actual");
    Long idBeneficiario = idBeneficiarioLogueado(context);

    // 1. Consultamos las entregas en Logística
    BeneficiarioEntregaRequest request =
        BeneficiarioEntregaRequest.newBuilder()
            .setIdBeneficiario(idBeneficiario)
            .build();

    ListaEntregasResponse response =
        logisticaStub.listarEntregasEnTraslado(request);

    // 2. Consultamos las donaciones asignadas al beneficiario
    ListaDonacionesBeneficiarioResponse responseDonaciones =
        donacionesStub.listarDonacionesPorBeneficiario(
            BeneficiarioIdRequest.newBuilder()
                .setId(idBeneficiario)
                .build()
        );

    // 3. Relacionamos cada ID de donación con su descripción
    Map<Long, String> descripciones = new HashMap<>();

    for (DonacionBeneficiarioMsg donacion : responseDonaciones.getDonacionesList()) {
      descripciones.put(donacion.getId(), donacion.getDescripcion());
    }

    // 4. Preparamos las entregas para la vista
    List<Map<String, Object>> entregasVista =
        response.getEntregasList().stream().map(entrega -> {

          Map<String, Object> datosEntrega = new HashMap<>();

          datosEntrega.put("id", entrega.getId());
          datosEntrega.put("estadoActual", entrega.getEstadoActual());

          List<Map<String, Object>> donacionesVista =
              entrega.getIdsDonacionesList().stream().map(idDonacion -> {

                Map<String, Object> datosDonacion = new HashMap<>();

                datosDonacion.put("id", idDonacion);
                datosDonacion.put(
                    "descripcion",
                    descripciones.getOrDefault(
                        idDonacion,
                        "Descripción no disponible"
                    )
                );

                return datosDonacion;
              }).toList();

          datosEntrega.put("donaciones", donacionesVista);

          return datosEntrega;
        }).toList();

    // 5. Enviamos los datos a Handlebars
    Map<String, Object> modelo = new HashMap<>();

    modelo.put("usuario", usuario);
    modelo.put("entregas", entregasVista);

    context.render("logistica/views/mis_entregas.hbs", modelo);
  }

  public void mostrarFormularioConfirmacion(Context context) {

    Long id = Long.valueOf(context.pathParam("id"));

    Long idBeneficiario = idBeneficiarioLogueado(context);

    EntregaIdRequest request = EntregaIdRequest.newBuilder()
        .setId(id)
        .setIdBeneficiario(idBeneficiario)
        .build();

    EntregaVistaMsg entrega;

    try {
      entrega = logisticaStub.obtenerEntregaPorId(request);

    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
        throw new ForbiddenResponse(
            "No tenés permiso para acceder a esta entrega"
        );
      }

      throw e;
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("entrega", entrega);

    context.render("logistica/views/confirmar_recepcion.hbs", modelo);
  }

  //Subo fotos pero no renderizo vista nueva. Sucede en la vista de confirmar_recepcion
  public void confirmarRecepcion(Context context) {
    Long id = Long.valueOf(context.pathParam("id"));

    Long idBeneficiario = idBeneficiarioLogueado(context);

    List<UploadedFile> fotosSubidas = context.uploadedFiles("fotos");
    checkQueTengaFotos(fotosSubidas);
    checkQueSeaFotosValida(fotosSubidas);

    ConfirmarRecepcionRequest.Builder requestBuilder =
        ConfirmarRecepcionRequest.newBuilder()
            .setIdEntrega(id)
            .setIdBeneficiario(idBeneficiario);

    try {
      for(UploadedFile foto : fotosSubidas){
        byte[] bytes = foto.content().readAllBytes();
        requestBuilder.addFotos(FotoRecepcionMsg.newBuilder()
            .setNombreArchivo(foto.filename())
            .setContenido(ByteString.copyFrom(bytes))
            .build());
      }
    } catch(IOException e) {
      throw new RuntimeException("Error al leer las fotos subidas", e);
    }

    try {
      logisticaStub.confirmarRecepcion(requestBuilder.build());

    } catch (StatusRuntimeException e) {

      if (e.getStatus().getCode() == Status.Code.PERMISSION_DENIED) {
        throw new ForbiddenResponse(
            "No tenés permiso para confirmar esta entrega"
        );
      }

      throw e;
    }

    context.redirect("/mis-entregas");

  }

  //=============== FUNCIONES AUXILIARES ===================
  private void checkQueTengaFotos(List<UploadedFile> fotos){
    if(fotos == null || fotos.isEmpty()){
      throw new RuntimeException("Error: Debe adjuntar al menos una fotografía de la recepción.");
    }
  }

  private void checkQueSeaFotosValida(List<UploadedFile> fotos){
    List<String> tipoImagenesPermitidas = List.of("image/jpeg", "image/png", "image/webp");

    for(UploadedFile foto : fotos){
      if (foto.contentType() == null || !tipoImagenesPermitidas.contains(foto.contentType())) {
        throw new RuntimeException("El archivo '" + foto.filename() + "' no es válido. " +
            "Solo se permiten imágenes del tipo: " + tipoImagenesPermitidas);
      }
    }
  }
  private Long idBeneficiarioLogueado(Context context) {
    Usuario usuario = context.sessionAttribute("usuario_actual");

    if (usuario == null) {
      throw new io.javalin.http.UnauthorizedResponse(
          "Necesitás iniciar sesión"
      );
    }

    if (usuario.getIdBeneficiario() == null) {
      throw new io.javalin.http.ForbiddenResponse(
          "El usuario no tiene una entidad beneficiaria asociada"
      );
    }

    return usuario.getIdBeneficiario();
  }


}

