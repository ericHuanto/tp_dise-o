package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.usuario.dominio.vista.Opcion;
import ar.edu.utn.frba.dds.donatrack.grpc.*;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import io.javalin.http.Context;


public class RegistroEntregaController implements WithSimplePersistenceUnit {
  private final DonacionesServiceBlockingStub donacionesStub;

  public RegistroEntregaController(DonacionesServiceBlockingStub donacionesStub) {
    this.donacionesStub = donacionesStub;
  }

  public void mostrarFormulario(Context context) {
    Usuario usuarioActual = context.sessionAttribute("usuario_actual");

    ListaSubcategoriasResponse response = donacionesStub.listarSubcategorias(Vacio.getDefaultInstance());

    Map<String, List<String>> catalogo = new TreeMap<>();
    for(SubcategoriaMsg s : response.getSubcategoriasList()) {
      catalogo.computeIfAbsent(s.getCategoria().getNombre(), k -> new ArrayList<>()).add(s.getNombre());
    }
    catalogo.values().forEach(Collections::sort);

    List<Map<String, Object>> categorias = catalogo.entrySet().stream()
        .map(e -> Map.<String, Object>of("nombre", e.getKey(), "subcategorias", e.getValue()))
        .toList();

    List<Opcion> unidades = Arrays.stream(UnidadMedidaProto.values())
        .filter(u -> u != UnidadMedidaProto.UNRECOGNIZED)
        .map(u -> new Opcion(u.name(), u.toString().toLowerCase()))
        .toList();

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuarioActual);
    modelo.put("hoy", LocalDate.now().toString());
    modelo.put("categorias", categorias);
    modelo.put("unidades", unidades);

    context.render("donaciones/views/admin/registrar_entrega.hbs", modelo);
  }
}