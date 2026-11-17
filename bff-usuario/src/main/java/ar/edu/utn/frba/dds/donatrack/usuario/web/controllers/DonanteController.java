package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import io.javalin.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonanteController {
  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub;

  public DonanteController(DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void donacionesPropias(Context context) {
    Long idDonante = idDonanteLogueado(context);
    if (idDonante == null) {
      return;
    }

    String estado; //revisar
    Long idCategoria;
    List<Long> idsSubcategorias;
    try {
      String estadoParam = context.queryParam("estado"); //revisar
      estado = (estadoParam == null || estadoParam.isBlank()) ? null : estadoParam;

      String categoriaParam = context.queryParam("categoria");
      idCategoria = (categoriaParam == null || categoriaParam.isBlank()) ? null : Long.valueOf(categoriaParam);

      idsSubcategorias = context.queryParamsAsClass("subcategoria", Long.class).getOrDefault(List.of());
    } catch (IllegalArgumentException | ValidationException e) {
      context.redirect("/donante/donaciones");
      return;
    }

    try {
      FiltroDonacionesDonanteRequest request = FiltroDonacionesDonanteRequest.newBuilder()
          .setIdDonante(idDonante)
          .setEstado(estado != null ? estado : "")
          .setIdCategoria(idCategoria != null ? idCategoria : 0L)
          .addAllIdsSubcategorias(idsSubcategorias)
          .build();

      VistaDonacionesDonanteResponse response = donacionesStub.obtenerVistaDonacionesDonante(request);

      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", context.sessionAttribute("usuario_actual"));
      modelo.put("donaciones", response.getDonacionesList());
      modelo.put("estados", response.getEstadosList());
      modelo.put("categorias", response.getCategoriasList());
      modelo.put("subcategorias", response.getSubcategoriasList());
      modelo.put("estadoElegido", estado);
      modelo.put("categoriaElegida", idCategoria);
      modelo.put("subcategoriasElegidas", idsSubcategorias);

      context.render("donaciones/views/donante/donaciones_donante.hbs", modelo);
    } catch (StatusRuntimeException e) {
      context.redirect("/donante/donaciones");
    }
  }

  public void beneficiarios(Context context) {
    if (idDonanteLogueado(context) == null) {
      return;
    }

    ListaBeneficiariosParaDonanteResponse response = donacionesStub.listarBeneficiariosParaDonante(
        Vacio.getDefaultInstance()
    );

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("beneficiarios", response.getBeneficiariosList());

    context.render("donaciones/views/donante/beneficiarios.hbs", modelo);
  }

  public void detalleBeneficiario(Context context) {
    if (idDonanteLogueado(context) == null) {
      return;
    }

    Long id;
    try {
      id = context.pathParamAsClass("id", Long.class).get();
    } catch (ValidationException e) {
      context.redirect("/donante/beneficiarios");
      return;
    }

    DetalleBeneficiarioResponse response = donacionesStub.obtenerDetalleBeneficiario(
        BeneficiarioIdRequest.newBuilder().setId(id).build()
    );

    if (!response.getEncontrado()) {
      context.redirect("/donante/beneficiarios");
      return;
    }

    BeneficiarioDetalleMsg b = response.getBeneficiario();
    Map<String, Object> beneficiarioVista = Map.of(
        "id", b.getId(),
        "razonSocial", b.getRazonSocial(),
        "direccion", b.getDireccion(),
        "necesidades", b.getNecesidadesList()
    );

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    modelo.put("beneficiario", beneficiarioVista);

    context.render("donaciones/views/donante/detalle_beneficiario.hbs", modelo);
  }

  //================== FUNCIONES AUXILIARES ========================
  private Long idDonanteLogueado(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) {
      context.redirect("/login");
      return null;
    }
    if (usuarioActual.getIdDonante() == null) {
      context.redirect("/");
      return null;
    }
    return usuarioActual.getIdDonante();
  }
}