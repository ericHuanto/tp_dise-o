package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controller;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.Documento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.TipoDocumento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.Genero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.persona.Humana;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.MedioContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.BeneficiarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.DonanteRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.persistencia.UsuarioRepository;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.convers.ContactoMapper;
import ar.edu.utn.frba.dds.donatrack.donaciones.web.shared.dto.contacto.ContactoDto;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.javalin.http.Context;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilController implements WithSimplePersistenceUnit {
  private DonanteRepository repoDonantes;
  private BeneficiarioRepository repoBeneficiarios;
  private UsuarioRepository repoUsuarios;

  public PerfilController(DonanteRepository repoDonantes, BeneficiarioRepository repoBeneficiarios, UsuarioRepository repoUsuarios) {
    this.repoDonantes = repoDonantes;
    this.repoBeneficiarios = repoBeneficiarios;
    this.repoUsuarios = repoUsuarios;
  }

  public void mostrarOpcionesPerfil(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    context.render("registro_elegir_perfil.hbs", modelo);
  }

  public void mostrarFormularioRegistroBeneficiario(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    context.render("registro_beneficiario.hbs", modelo);
  }

  public void mostrarFormularioRegistroDonanteHumano(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("tiposDocumento", TipoDocumento.valoresPosiblesHumana());
    modelo.put("generos", Genero.values());
    modelo.put("usuario", context.sessionAttribute("usuario_actual"));
    context.render("registro_persona_humana.hbs", modelo);
  }

  public void guardarBeneficiario(Context context) {
    String razonSocial = context.formParam("razonSocial");
    String direccion = context.formParam("direccion");
    List<MedioContacto> contactos = armarContactos(context);

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) throw new RuntimeException("Este mensaje no deberías verlo nunca");
    Beneficiario nuevoBeneficiario = new Beneficiario(razonSocial, direccion, contactos);

    usuarioActual.otorgarPermiso(Permiso.SOLICITAR_AYUDA);
    nuevoBeneficiario.vincularUsuario(usuarioActual);

    try {
      beginTransaction();
      repoBeneficiarios.guardar(nuevoBeneficiario);
      repoUsuarios.actualizar(usuarioActual);
      commitTransaction();

      context.sessionAttribute("usuario_actual", usuarioActual);
      context.redirect("/");
    } catch (Exception e) {
      e.printStackTrace();
      rollbackTransaction();
      context.redirect("/perfil/beneficiario?error=Error_interno_al_guardar");
    }
  }

  public void guardarDonanteHumana(Context context) {
    String nombre = context.formParam("nombre");
    String apellidos = context.formParam("apellidos");
    TipoDocumento tipoDoc = TipoDocumento.valueOf(context.formParam("tipoDocumento"));
    String numDocumento = context.formParam("numeroDocumento");
    LocalDate fechaNac = LocalDate.parse(context.formParam("fechaNacimiento"));
    Genero genero = Genero.valueOf(context.formParam("genero"));
    String direccion = context.formParam("direccion");
    List<MedioContacto> contactos = armarContactos(context);

    Usuario usuarioActual = context.sessionAttribute("usuario_actual");
    if (usuarioActual == null) throw new RuntimeException("Este mensaje no deberías verlo nunca");
    Humana nuevoDonanteHumana = new Humana(nombre, apellidos, new Documento(tipoDoc, numDocumento), fechaNac, genero, direccion, contactos);

    usuarioActual.otorgarPermiso(Permiso.REALIZAR_DONACION);
    nuevoDonanteHumana.vincularUsuario(usuarioActual);

    try {
      beginTransaction();
      repoDonantes.guardar(nuevoDonanteHumana);
      repoUsuarios.actualizar(usuarioActual);
      commitTransaction();

      context.sessionAttribute("usuario_actual", usuarioActual);
      context.redirect("/");
    } catch (Exception e) {
      e.printStackTrace();
      rollbackTransaction();
      context.redirect("/perfil/donante-humana?error=Error_interno_al_guardar");
    }
  }

  // =================== FUNCIONES AUXILIARES ===================
  private List<MedioContacto> armarContactos(Context context) {
    List<MedioContacto> todosLosContactos = new ArrayList<>();

    todosLosContactos.add(armarContactoPrincipal(context.formParam("medio_principal"), context.formParam("valor_principal")));
    todosLosContactos.addAll(armarContactosOpcionales(context.formParams("medios[]"), context.formParams("valores[]"), context.formParams("principales[]")));

    return todosLosContactos;
  }

  private MedioContacto armarContactoPrincipal(String tipoContacto, String valor) {
    ContactoDto dto = new ContactoDto(tipoContacto, valor, true);
    return ContactoMapper.aDominio(dto);
  }

  private List<MedioContacto> armarContactosOpcionales(List<String> tiposContacto, List<String> valores, List<String> SonPrincipales) {
    if (tiposContacto == null || tiposContacto.isEmpty()) {
      return new ArrayList<>();
    }

    List<ContactoDto> dtos = new ArrayList<>();
    for (int i = 0; i < tiposContacto.size(); i++) {
      boolean esPrincipal = Boolean.parseBoolean(SonPrincipales.get(i));
      dtos.add(new ContactoDto(tiposContacto.get(i), valores.get(i), esPrincipal));
    }

    return ContactoMapper.aDominio(dtos);
  }

}