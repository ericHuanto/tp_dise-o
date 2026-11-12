package ar.edu.utn.frba.dds.donatrack.donaciones.web.ui.controllers;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.Documento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.TipoDocumento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.Genero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.juridica.Juridica;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.juridica.Representante;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.juridica.TipoOrganizacion;
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

  public void mostrarFormularioRegistroBeneficiario(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    context.render("views/auth/registro_beneficiario.hbs", modelo);
  }

  public void mostrarFormularioRegistroDonanteHumano(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("tiposDocumento", TipoDocumento.valoresPosiblesHumana());
    modelo.put("generos", Genero.values());
    context.render("views/auth/registro_persona_humana.hbs", modelo);
  }

  public void mostrarFormularioRegistroDonanteJuridica(Context context) {
    Map<String, Object> modelo = new HashMap<>();
    modelo.put("tiposDocumento", TipoDocumento.valoresPosiblesJuridica());
    modelo.put("tiposDocumentoRepresentante", TipoDocumento.valoresPosiblesHumana());
    modelo.put("tiposOrganizacion", TipoOrganizacion.values());
    context.render("views/auth/registro_persona_juridica.hbs", modelo);
  }

  public void guardarBeneficiario(Context context) {
    if(existeNombreUsuario(context.formParam("name"))) {
      context.redirect("/perfil/beneficiario?error=ya_existe_un_usuario_con_ese_nombre");
      return;
    }

    if(!esMismaContraseña(context.formParam("password"), context.formParam("passwordConfirm"))) {
      context.redirect("/perfil/beneficiario?error=las_contraseñas_no_coinciden");
      return;
    }

    Usuario nuevoUsuario = crearUsuario(context.formParam("name"), context.formParam("password"));
    Beneficiario nuevoBeneficiario = crearBeneficiario(context);

    nuevoUsuario.otorgarPermiso(Permiso.SOLICITAR_AYUDA);
    nuevoBeneficiario.vincularUsuario(nuevoUsuario);

    try {
      beginTransaction();
      repoUsuarios.guardar(nuevoUsuario);
      repoBeneficiarios.guardar(nuevoBeneficiario);
      commitTransaction();

      nuevoUsuario.inicializarPermisos();
      context.sessionAttribute("usuario_actual", nuevoUsuario);
      context.redirect("/");
    } catch (Exception e) {
      e.printStackTrace();
      rollbackTransaction();
      context.redirect("/perfil/beneficiario?error=Error_interno_al_guardar");
    }
  }

  public void guardarDonanteHumana(Context context) {
    if(existeNombreUsuario(context.formParam("name"))) {
      context.redirect("/perfil/donante-humana?error=ya_existe_un_usuario_con_ese_nombre");
      return;
    }

    if(!esMismaContraseña(context.formParam("password"), context.formParam("passwordConfirm"))) {
      context.redirect("/perfil/donante-humana?error=las_contraseñas_no_coinciden");
      return;
    }

    Usuario nuevoUsuario = crearUsuario(context.formParam("name"), context.formParam("password"));
    Humana nuevoDonanteHumana = crearDonanteHumana(context);

    nuevoUsuario.otorgarPermiso(Permiso.REALIZAR_DONACION);
    nuevoDonanteHumana.vincularUsuario(nuevoUsuario);

    try {
      beginTransaction();
      repoUsuarios.guardar(nuevoUsuario);
      repoDonantes.guardar(nuevoDonanteHumana);
      commitTransaction();

      nuevoUsuario.inicializarPermisos();
      context.sessionAttribute("usuario_actual", nuevoUsuario);
      context.redirect("/");
    } catch (Exception e) {
      e.printStackTrace();
      rollbackTransaction();
      context.redirect("/perfil/donante-humana?error=Error_interno_al_guardar");
    }
  }

  public void guardarDonanteJuridica(Context context) {
    if(existeNombreUsuario(context.formParam("name"))) {
      context.redirect("/perfil/donante-juridica?error=ya_existe_un_usuario_con_ese_nombre");
      return;
    }

    if(!esMismaContraseña(context.formParam("password"), context.formParam("passwordConfirm"))) {
      context.redirect("/perfil/donante-juridica?error=las_contraseñas_no_coinciden");
      return;
    }

    Usuario nuevoUsuario = crearUsuario(context.formParam("name"), context.formParam("password"));
    Juridica nuevajuridica = crearDonanteJuridica(context);

    nuevoUsuario.otorgarPermiso(Permiso.REALIZAR_DONACION);
    nuevajuridica.vincularUsuario(nuevoUsuario);

    try {
      beginTransaction();
      repoUsuarios.guardar(nuevoUsuario);
      repoDonantes.guardar(nuevajuridica);
      commitTransaction();

      nuevoUsuario.inicializarPermisos();
      context.sessionAttribute("usuario_actual", nuevoUsuario);
      context.redirect("/");
    } catch (Exception e) {
      e.printStackTrace();
      rollbackTransaction();
      context.redirect("/perfil/donante-juridica?error=Error_interno_al_guardar");
    }
  }

  // =================== FUNCIONES AUXILIARES ===================
  private boolean esMismaContraseña(String contraseña, String contraseña2) {
    return contraseña.equals(contraseña2);
  }

  private boolean existeNombreUsuario(String nombreUsuario) {
    return repoUsuarios.buscarPorCredenciales(nombreUsuario) != null;
  }

  private Usuario crearUsuario(String nombreUsuario, String contraseña) {
    return new Usuario(nombreUsuario, contraseña);
  }

  private Beneficiario crearBeneficiario(Context context) {
    String razonSocial = context.formParam("razonSocial");
    String direccion = context.formParam("direccion");
    List<MedioContacto> contactos = armarContactos(context);

    return new Beneficiario(razonSocial, direccion, contactos);
  }

  private Humana crearDonanteHumana(Context context) {
    String nombre = context.formParam("nombre");
    String apellidos = context.formParam("apellidos");
    TipoDocumento tipoDocumento = TipoDocumento.valueOf(context.formParam("tipoDocumento"));
    String numDocumento = context.formParam("numeroDocumento");
    LocalDate fechaNacimiento = LocalDate.parse(context.formParam("fechaNacimiento"));
    Genero genero = Genero.valueOf(context.formParam("genero"));
    String direccion = context.formParam("direccion");
    List<MedioContacto> contactos = armarContactos(context);

    return new Humana(nombre, apellidos, new Documento(tipoDocumento, numDocumento), fechaNacimiento, genero, direccion, contactos);
  }

  private Juridica crearDonanteJuridica(Context context) {
    String razonSocial = context.formParam("razonSocial");
    TipoDocumento tipoDocJuridica = TipoDocumento.valueOf(context.formParam("tipoDocumento"));
    String numDocJuridica = context.formParam("numeroDocumento");
    TipoOrganizacion tipoOrg = TipoOrganizacion.valueOf(context.formParam("tipoOrganizacion"));
    String rubro = context.formParam("rubro");
    List<Representante> representantes = List.of(crearRepresentante(context));

    return new Juridica(razonSocial, new Documento(tipoDocJuridica, numDocJuridica), tipoOrg, rubro, representantes);
  }

  private Representante crearRepresentante(Context context) {
    String nombre = context.formParam("nombreRepresentante");
    String apellido = context.formParam("apellidoRepresentante");
    TipoDocumento tipoDocumento = TipoDocumento.valueOf(context.formParam("tipoDocumentoRepresentante"));
    String numDocumento = context.formParam("numeroDocumentoRepresentante");
    String direccion = context.formParam("direccionRepresentante");
    List<MedioContacto> contactos = armarContactos(context);

    return new Representante(nombre, apellido, new Documento(tipoDocumento, numDocumento), direccion, contactos);
  }

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