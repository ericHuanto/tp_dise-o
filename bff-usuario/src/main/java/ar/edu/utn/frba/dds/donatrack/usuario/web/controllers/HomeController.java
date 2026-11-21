package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.Vacio;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.javalin.http.Context;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HomeController {
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private final DonacionesServiceBlockingStub donacionesStub;

  public HomeController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarLanding(Context context) {
    if (context.sessionAttribute("usuario_actual") != null) {
      context.redirect("/home");
      return;
    }
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("ultimasEntregas", ultimasEntregas(4));
    context.render("comun/views/landing.hbs", modelo);
  }

  public void mostrarHome(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    Map<String, Object> modelo = new HashMap<>();

    if (usuarioActual != null) {
      modelo.put("usuario", usuarioActual);
      context.render("comun/views/home.hbs", modelo);
    } else {
      context.render("comun/views/landing.hbs");
    }
  }

  //=============== FUNCIONES AUXILIARES ===================
  private List<Map<String, Object>> ultimasEntregas(int cantidad) {
    try {
      String json = donacionesStub.withDeadlineAfter(2, TimeUnit.SECONDS)
          .obtenerDatosMapaDonacionesRecibidas(Vacio.getDefaultInstance())
          .getMarcadoresJson();
      List<Map<String, Object>> entregas = new Gson()
          .fromJson(json, new TypeToken<List<Map<String, Object>>>() { }.getType());

      return entregas.stream()
          .sorted(Comparator.comparing(this::fechaDeEntrega).reversed())
          .limit(cantidad)
          .map(this::aItemLanding)
          .toList();
    } catch (Exception e) {
      return List.of(); // si Donaciones no responde, la landing carga igual
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> aItemLanding(Map<String, Object> entrega) {
    List<Map<String, Object>> bienes =
        (List<Map<String, Object>>) entrega.getOrDefault("donaciones", List.of());
    List<String> fotos = (List<String>) entrega.getOrDefault("fotos", List.of());

    Map<String, Object> item = new HashMap<>();
    item.put("entidad", entrega.get("razonSocial"));
    item.put("bienes", bienes.stream()
        .map(b -> String.valueOf(b.get("descripcion")))
        .collect(Collectors.joining(", ")));
    item.put("fecha", entrega.get("fechaEntrega"));
    item.put("foto", fotos.isEmpty() ? null : fotos.get(0));
    return item;
  }

  private LocalDate fechaDeEntrega(Map<String, Object> entrega) {
    try {
      return LocalDate.parse(String.valueOf(entrega.get("fechaEntrega")), FECHA);
    } catch (Exception e) {
      return LocalDate.MIN; // sin fecha: va al final
    }
  }
}