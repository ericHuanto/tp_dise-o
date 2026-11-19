package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.BeneficiarioIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.ContactoMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc;
import ar.edu.utn.frba.dds.donatrack.grpc.DonanteIdRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.PerfilBeneficiarioResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.PerfilDonanteResponse;
import ar.edu.utn.frba.dds.donatrack.grpc.RepresentanteMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.ResumenDonacionesMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.UltimaActividadMsg;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.generadorhash.Integridad;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.UsuarioRepository;
import ar.edu.utn.frba.dds.donatrack.usuario.web.dto.PerfilVista;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.grpc.StatusRuntimeException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class MiPerfilController implements WithSimplePersistenceUnit {
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub;
  private final UsuarioRepository repoUsuarios;

  public MiPerfilController(DonacionesServiceGrpc.DonacionesServiceBlockingStub donacionesStub, UsuarioRepository repoUsuarios) {
    this.donacionesStub = donacionesStub;
    this.repoUsuarios = repoUsuarios;
  }

  public void mostrarPerfil(Context context) {
    Usuario usuario = context.sessionAttribute("usuario_actual");   // el id sale de la sesión, nunca de la URL
    PerfilVista perfil = new PerfilVista();

    if (usuario.getIdDonante() != null) cargarDonante(usuario.getIdDonante(), perfil);
    if (usuario.getIdBeneficiario() != null) cargarBeneficiario(usuario.getIdBeneficiario(), perfil);
    if (usuario.isAdmin()) perfil.agregarEtiqueta("ADMINISTRACIÓN");
    perfil.cerrar(usuario.getNombreUsuario());

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", usuario);
    modelo.put("perfil", perfil);
    modelo.put("permisos", usuario.getPermisos().stream().map(Enum::name).toList());
    modelo.put("cambioOk", "ok".equals(context.queryParam("cambio")));
    context.render("donaciones/views/perfil/mi_perfil.hbs", modelo);
  }

  public void mostrarFormularioContrasena(Context context) {
    context.render("donaciones/views/perfil/cambiar_contrasena.hbs",
        Map.of("usuario", context.sessionAttribute("usuario_actual")));
  }

  public void cambiarContrasena(Context context) {
    Usuario sesion = context.sessionAttribute("usuario_actual");
    String actual = context.formParam("actual");
    String nueva = context.formParam("nueva");
    String confirmar = context.formParam("confirmar");

    String error;
    if (actual == null || nueva == null || !nueva.equals(confirmar)) {
      error = "Las contraseñas nuevas no coinciden";
    } else if (!Integridad.validar(actual, sesion.getContraseña())) {
      error = "La contraseña actual es incorrecta";
    } else if (actual.equals(nueva)) {
      error = "La nueva contraseña debe ser distinta de la actual";
    } else {
      error = validarPolitica(nueva);   // null si cumple
    }

    if (error != null) {
      Map<String, Object> modelo = new HashMap<>();
      modelo.put("usuario", sesion);
      modelo.put("error", error);
      context.status(400).render("donaciones/views/perfil/cambiar_contrasena.hbs", modelo);
      return;
    }

    Usuario usuario = repoUsuarios.buscarPorId(sesion.getId());
    usuario.cambiarContrasena(nueva);
    beginTransaction();
    repoUsuarios.actualizar(usuario);
    commitTransaction();

    context.req().changeSessionId();
    context.redirect("/perfil?cambio=ok");
  }

  // Devuelve el mensaje de error, o null si la clave cumple la política.
  private static String validarPolitica(String clave) {
    if (clave.length() < 8) return "La contraseña debe tener al menos 8 caracteres";
    if (clave.length() > 72) return "La contraseña no puede superar los 72 caracteres";
    if (clave.chars().noneMatch(Character::isDigit)) return "La contraseña debe incluir al menos un número";
    return null;
  }

  //============= DATOS POR PERFIL =====================
  private void cargarDonante(Long id, PerfilVista perfil) {
    try {
      PerfilDonanteResponse d = donacionesStub.withDeadlineAfter(3, TimeUnit.SECONDS)
          .obtenerPerfilDonante(DonanteIdRequest.newBuilder().setId(id).build());
      boolean humana = "HUMANA".equals(d.getTipoPersona());

      perfil.definirNombre(humana ? d.getNombre() + " " + d.getApellido() : d.getNombre());
      perfil.agregarEtiqueta(humana ? "PERSONA HUMANA" : "PERSONA JURÍDICA");
      perfil.agregarResumen(resumen("Resumen de donaciones", d.getResumen(), "Donaciones", "Entregadas"));

      UltimaActividadMsg u = d.getUltimaEntrega();
      if (u.getExiste()) {
        perfil.agregarDestacado(new PerfilVista.Destacado("Última entrega", fecha(u.getFecha()),
            recortar(u.getDescripcion(), 90), u.getCantidadBienes() + " bienes · " + haceDias(u.getFecha())));
      }

      String contacto = contactoPrincipal(d.getContactosList());
      if (humana) {
        perfil.agregarSeccion(new PerfilVista.Seccion("Datos personales", List.of(
            campo("Nombre", d.getNombre()), campo("Apellidos", d.getApellido()),
            campo("Documento", d.getTipoDocumento() + " " + d.getNumeroDocumento()),
            campo("Fecha de nacimiento", fechaConEdad(d.getFechaNacimiento())),
            campo("Género", legible(d.getGenero())), campo("Dirección", d.getDireccion()),
            campo("Contacto principal", contacto))));
      } else {
        RepresentanteMsg r = d.getRepresentante();
        perfil.agregarSeccion(new PerfilVista.Seccion("Datos de la organización", List.of(
            campo("Razón social", d.getNombre()),
            campo("Documento", d.getTipoDocumento() + " " + d.getNumeroDocumento()),
            campo("Tipo de organización", legible(d.getTipoOrganizacion())), campo("Rubro", d.getRubro()),
            campo("Contacto principal", contacto))));
        perfil.agregarSeccion(new PerfilVista.Seccion("Representante", List.of(
            campo("Nombre", r.getNombre() + " " + r.getApellido()),
            campo("Documento", r.getTipoDocumento() + " " + r.getNumeroDocumento()),
            campo("Dirección", r.getDireccion()))));
      }
    } catch (StatusRuntimeException | DateTimeException e) {
      System.err.println("[Perfil] donante " + id + ": " + e.getMessage());
      perfil.agregarAviso("No pudimos cargar tus datos de donante. Probá de nuevo en unos minutos.");
    }
  }

  private void cargarBeneficiario(Long id, PerfilVista perfil) {
    try {
      PerfilBeneficiarioResponse b = donacionesStub.withDeadlineAfter(3, TimeUnit.SECONDS)
          .obtenerPerfilBeneficiario(BeneficiarioIdRequest.newBuilder().setId(id).build());

      perfil.definirNombre(b.getRazonSocial());
      perfil.agregarEtiqueta("ENTIDAD BENEFICIARIA");

      ResumenDonacionesMsg r = b.getResumen();
      perfil.agregarResumen(new PerfilVista.Resumen("Resumen de donaciones recibidas", List.of(
          new PerfilVista.Dato(String.valueOf(r.getTotal()), "Asignadas"),
          new PerfilVista.Dato(String.valueOf(r.getEntregadas()), "Recibidas"),
          new PerfilVista.Dato(String.valueOf(r.getEnCurso()), "En curso"),
          new PerfilVista.Dato(String.valueOf(b.getNecesidadesAbiertas()), "Necesidades abiertas"))));

      UltimaActividadMsg u = b.getUltimaDonacion();
      if (u.getExiste()) {
        perfil.agregarDestacado(new PerfilVista.Destacado("Última donación recibida", fecha(u.getFecha()),
            recortar(u.getDescripcion(), 90), legible(u.getEstado()) + " · " + haceDias(u.getFecha())));
      }

      perfil.agregarSeccion(new PerfilVista.Seccion("Datos de la entidad", List.of(
          campo("Razón social", b.getRazonSocial()), campo("Dirección", b.getDireccion()),
          campo("Contacto principal", contactoPrincipal(b.getContactosList())))));
    } catch (StatusRuntimeException | DateTimeException e) {
      System.err.println("[Perfil] beneficiario " + id + ": " + e.getMessage());
      perfil.agregarAviso("No pudimos cargar los datos de tu entidad. Probá de nuevo en unos minutos.");
    }
  }

  //============= FUNCIONES AUXILIARES =====================

  private static PerfilVista.Resumen resumen(String titulo, ResumenDonacionesMsg r, String total, String entregadas) {
    List<PerfilVista.Dato> items = new ArrayList<>(List.of(
        new PerfilVista.Dato(String.valueOf(r.getTotal()), total),
        new PerfilVista.Dato(String.valueOf(r.getEntregadas()), entregadas),
        new PerfilVista.Dato(String.valueOf(r.getEnCurso()), "En curso"),
        new PerfilVista.Dato(String.valueOf(r.getFallidas()), "Entrega fallida")));
    if (r.getVencidas() > 0) items.add(new PerfilVista.Dato(String.valueOf(r.getVencidas()), "Vencidas"));
    return new PerfilVista.Resumen(titulo, items, "Ver mis donaciones →", "/mis-donaciones");
  }

  private static PerfilVista.Campo campo(String etiqueta, String valor) {
    return new PerfilVista.Campo(etiqueta, valor == null || valor.isBlank() ? "-" : valor.trim());
  }

  private static String contactoPrincipal(List<ContactoMsg> contactos) {
    return contactos.stream().filter(ContactoMsg::getEsPrincipal).findFirst()
        .or(() -> contactos.stream().findFirst())
        .map(ContactoMsg::getValor).orElse("-");
  }

  private static String fecha(String iso) {
    return iso == null || iso.isBlank() ? "-" : LocalDate.parse(iso).format(FECHA);
  }

  private static String fechaConEdad(String iso) {
    if (iso == null || iso.isBlank()) return "-";
    LocalDate f = LocalDate.parse(iso);
    return f.format(FECHA) + " (" + Period.between(f, LocalDate.now()).getYears() + " años)";
  }

  private static String haceDias(String iso) {
    long d = ChronoUnit.DAYS.between(LocalDate.parse(iso), LocalDate.now());
    return d <= 0 ? "hoy" : d == 1 ? "hace 1 día" : "hace " + d + " días";
  }

  private static String legible(String enumTexto) {
    if (enumTexto == null || enumTexto.isBlank()) return "-";
    String t = enumTexto.toLowerCase().replace('_', ' ');
    return Character.toUpperCase(t.charAt(0)) + t.substring(1);
  }

  private static String recortar(String texto, int max) {
    return texto != null && texto.length() > max ? texto.substring(0, max - 1) + "…" : texto;
  }
}
