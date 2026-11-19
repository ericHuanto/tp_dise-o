package ar.edu.utn.frba.dds.donatrack.usuario.web.controllers;

import ar.edu.utn.frba.dds.donatrack.grpc.ContactoMsg;
import ar.edu.utn.frba.dds.donatrack.grpc.DonacionesServiceGrpc.DonacionesServiceBlockingStub;
import ar.edu.utn.frba.dds.donatrack.grpc.RegistrarBeneficiarioRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.RegistrarHumanaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.RegistrarJuridicaRequest;
import ar.edu.utn.frba.dds.donatrack.grpc.RegistroActorResponse;
import ar.edu.utn.frba.dds.donatrack.usuario.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.usuario.persistencia.UsuarioRepository;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.grpc.StatusRuntimeException;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilController implements WithSimplePersistenceUnit {
  private final DonacionesServiceBlockingStub donacionesStub;
  private final UsuarioRepository repoUsuarios;

  public PerfilController(DonacionesServiceBlockingStub donacionesStub, UsuarioRepository repoUsuarios) {
    this.donacionesStub = donacionesStub;
    this.repoUsuarios = repoUsuarios;
  }

  public void mostrarFormularioRegistroBeneficiario(Context context) {
    context.render("comun/views/auth/registro_beneficiario.hbs", new HashMap<>());
  }

  public void mostrarFormularioRegistroDonanteHumano(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("tiposDocumento", List.of("DNI", "CUIT", "PASAPORTE"));
    modelo.put("generos", List.of("FEMENINO", "MASCULINO", "X", "SIN_ESPECIFICAR"));
    context.render("comun/views/auth/registro_persona_humana.hbs", modelo);
  }

  public void mostrarFormularioRegistroDonanteJuridica(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("tiposDocumentoJuridica", List.of("CUIT"));
    modelo.put("tiposDocumentoHumana", List.of("DNI", "PASAPORTE"));
    modelo.put("tiposOrganizacion", List.of("GUBERNAMENTAL", "ONG", "EMPRESA", "INSTITUCION", "SIN_ESPECIFICAR"));
    context.render("comun/views/auth/registro_persona_juridica.hbs", modelo);
  }

  public void guardarBeneficiario(Context context) {
    Usuario nuevoUsuario = validarYConstruirUsuario(context, "/perfil/beneficiario");
    if (nuevoUsuario == null) return;

    try {
      RegistrarBeneficiarioRequest request = RegistrarBeneficiarioRequest.newBuilder()
          .setRazonSocial(context.formParam("razonSocial"))
          .setDireccion(context.formParam("direccion"))
          .addAllContactos(armarContactosProto(context))
          .build();

      // 1. Llamada gRPC al microservicio de donaciones
      RegistroActorResponse response = donacionesStub.registrarBeneficiario(request);

      // 2. Guardamos el Usuario en el BFF vinculado al id_beneficiario devuelto
      nuevoUsuario.vincularBeneficiario(response.getIdCreado());
      beginTransaction();
      repoUsuarios.guardar(nuevoUsuario);
      commitTransaction();

      nuevoUsuario.inicializarPermisos();
      context.sessionAttribute("usuario_actual", nuevoUsuario);
      context.redirect("/");
    } catch (StatusRuntimeException e) {
      rollbackSiActiva();
      String motivo = e.getStatus().getDescription() != null ? e.getStatus().getDescription() : "Error_en_datos";
      context.redirect("/perfil/beneficiario?error=" + motivo.replace(' ', '_'));
    } catch (Exception e) {
      e.printStackTrace();
      rollbackSiActiva();
      context.redirect("/perfil/beneficiario?error=Error_interno_al_guardar");
    }
  }

  public void guardarDonanteHumana(Context context) {
    Usuario nuevoUsuario = validarYConstruirUsuario(context, "/perfil/donante-humana");
    if (nuevoUsuario == null) return;

    try {
      RegistrarHumanaRequest request = RegistrarHumanaRequest.newBuilder()
          .setNombre(context.formParam("nombre"))
          .setApellido(context.formParam("apellidos"))
          .setTipoDocumento(context.formParam("tipoDocumento"))
          .setNumeroDocumento(context.formParam("numeroDocumento"))
          .setFechaNacimiento(context.formParam("fechaNacimiento"))
          .setGenero(context.formParam("genero"))
          .setDireccion(context.formParam("direccion"))
          .addAllContactos(armarContactosProto(context))
          .build();

      RegistroActorResponse response = donacionesStub.registrarDonanteHumana(request);

      nuevoUsuario.vincularDonante(response.getIdCreado());
      beginTransaction();
      repoUsuarios.guardar(nuevoUsuario);
      commitTransaction();

      nuevoUsuario.inicializarPermisos();
      context.sessionAttribute("usuario_actual", nuevoUsuario);
      context.redirect("/");
    } catch (StatusRuntimeException e) {
      rollbackSiActiva();
      String motivo = e.getStatus().getDescription() != null ? e.getStatus().getDescription() : "Error_en_datos";
      context.redirect("/perfil/donante-humana?error=" + motivo.replace(' ', '_'));
    } catch (Exception e) {
      e.printStackTrace();
      rollbackSiActiva();
      context.redirect("/perfil/donante-humana?error=Error_interno_al_guardar");
    }
  }

  public void guardarDonanteJuridica(Context context) {
    Usuario nuevoUsuario = validarYConstruirUsuario(context, "/perfil/donante-juridica");
    if (nuevoUsuario == null) return;

    try {
      RegistrarJuridicaRequest request = RegistrarJuridicaRequest.newBuilder()
          .setRazonSocial(context.formParam("razonSocial"))
          .setTipoDocumento(context.formParam("tipoDocumento"))
          .setNumeroDocumento(context.formParam("numeroDocumento"))
          .setTipoOrganizacion(context.formParam("tipoOrganizacion"))
          .setRubro(context.formParam("rubro"))
          .setNombreRepresentante(context.formParam("nombreRepresentante"))
          .setApellidoRepresentante(context.formParam("apellidoRepresentante"))
          .setTipoDocumentoRepresentante(context.formParam("tipoDocumentoRepresentante"))
          .setNumeroDocumentoRepresentante(context.formParam("numeroDocumentoRepresentante"))
          .setDireccionRepresentante(context.formParam("direccionRepresentante"))
          .addAllContactos(armarContactosProto(context))
          .build();

      RegistroActorResponse response = donacionesStub.registrarDonanteJuridica(request);

      nuevoUsuario.vincularDonante(response.getIdCreado());
      beginTransaction();
      repoUsuarios.guardar(nuevoUsuario);
      commitTransaction();

      nuevoUsuario.inicializarPermisos();
      context.sessionAttribute("usuario_actual", nuevoUsuario);
      context.redirect("/");
    } catch (StatusRuntimeException e) {
      rollbackSiActiva();
      String motivo = e.getStatus().getDescription() != null ? e.getStatus().getDescription() : "Error_en_datos";
      context.redirect("/perfil/donante-juridica?error=" + motivo.replace(' ', '_'));
    } catch (Exception e) {
      e.printStackTrace();
      rollbackSiActiva();
      context.redirect("/perfil/donante-juridica?error=Error_interno_al_guardar");
    }
  }

  // =================== FUNCIONES AUXILIARES ===================
  private Usuario validarYConstruirUsuario(Context context, String rutaFormulario) {
    String nombreUsuario = context.formParam("name");
    String password = context.formParam("password");
    String passwordConfirm = context.formParam("passwordConfirm");

    if (passwordConfirm != null && !password.equals(passwordConfirm)) {
      context.redirect(rutaFormulario + "?error=Las_contraseñas_no_coinciden");
      return null;
    }
    if (repoUsuarios.buscarPorCredenciales(nombreUsuario) != null) {
      context.redirect(rutaFormulario + "?error=El_nombre_de_usuario_ya_esta_en_uso");
      return null;
    }
    return new Usuario(nombreUsuario, password);
  }

  private List<ContactoMsg> armarContactosProto(Context context) {
    List<ContactoMsg> lista = new ArrayList<>();
    lista.add(ContactoMsg.newBuilder()
        .setTipoContacto(context.formParam("medio_principal"))
        .setValor(context.formParam("valor_principal"))
        .setEsPrincipal(true)
        .build());

    List<String> medios = context.formParams("medios[]");
    List<String> valores = context.formParams("valores[]");
    List<String> principales = context.formParams("principales[]");

    if (medios != null) {
      for (int i = 0; i < medios.size(); i++) {
        lista.add(ContactoMsg.newBuilder()
            .setTipoContacto(medios.get(i))
            .setValor(valores.get(i))
            .setEsPrincipal(Boolean.parseBoolean(principales.get(i)))
            .build());
      }
    }
    return lista;
  }

  private void rollbackSiActiva() {
    if (entityManager().getTransaction().isActive()) {
      rollbackTransaction();
    }
  }
}