package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.LogisticaServiceGrpc.LogisticaServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.*;
import com.google.protobuf.ByteString;
import io.javalin.http.UploadedFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.javalin.http.Context;


public class EntregaController {
  private final LogisticaServiceBlockingStub logisticaStub;

  public EntregaController(LogisticaServiceBlockingStub logisticaStub){
    this.logisticaStub = logisticaStub;
  }

  public void mostrarMisEntregas(Context context) {
    ListaEntregasResponse response = logisticaStub.listarEntregasEnTraslado(VacioLogistica.getDefaultInstance());

    Map<String,Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("entregas", response.getEntregasList());

    context.render("logistica/views/mis_entregas.hbs", modelo);
  }

  public void mostrarFormularioConfirmacion(Context context) {
    Long id = Long.valueOf(context.pathParam("id"));

    EntregaVistaMsg entrega = logisticaStub.obtenerEntregaPorId(
        EntregaIdRequest.newBuilder()
            .setId(id)
            .build()
    );

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("entrega", entrega); //Ver en el proto si queremos mostrar más

    context.render("logistica/views/confirmar_recepcion.hbs", modelo);
  }

  //Subo fotos pero no renderizo vista nueva. Sucede en la vista de confirmar_recepcion
  public void confirmarRecepcion(Context context) {
    Long id = Long.valueOf(context.pathParam("id"));

    List<UploadedFile> fotosSubidas = context.uploadedFiles("fotos");
    checkQueTengaFotos(fotosSubidas);
    checkQueSeaFotosValida(fotosSubidas);

    ConfirmarRecepcionRequest.Builder requestBuilder = ConfirmarRecepcionRequest.newBuilder()
        .setIdEntrega(id);

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

    logisticaStub.confirmarRecepcion(requestBuilder.build());

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


}

