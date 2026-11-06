package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Categoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.NoPerecedero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.Perecedero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.Documento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.TipoDocumento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.Genero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.persona.Humana;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.CorreoDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Frecuencia;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.LocalDate;
import java.util.List;

public class DatosIniciales implements WithSimplePersistenceUnit {

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
    DonanteRepository repoDonantes = DonanteRepository.getInstancia();
    CategoriaRepository repoCategorias = CategoriaRepository.getInstancia();
    SubcategoriaRepository repoSubcategorias = SubcategoriaRepository.getInstancia();
    DonacionRepository repoDonaciones = DonacionRepository.getInstancia();
    UsuarioRepository repoUsuarios = UsuarioRepository.getInstancia();

    Beneficiario eric = new Beneficiario("ericH", "siempre viva 123", List.of(new CorreoDeContacto("correoPrueba@gmail.com", true)));
    Humana juan = new Humana("Juan", "Perez", new Documento(TipoDocumento.DNI, "12345678"), LocalDate.now().minusYears(25), Genero.MASCULINO, "siempre viva 555", List.of(new CorreoDeContacto("correoJuan@gmail.com", true)));
    Humana andres = new Humana("andres", "Perez", new Documento(TipoDocumento.DNI, "12345678"), LocalDate.now().minusYears(25), Genero.MASCULINO, "siempre viva 555", List.of(new CorreoDeContacto("correoAndres@gmail.com", true)));
    Categoria alimentos = new Categoria("alimentos");
    Subcategoria lacteos = new Subcategoria("lacteos", alimentos);
    Subcategoria pastas = new Subcategoria("frutas", alimentos);
    Bien fideos = new Perecedero("Fideos secos", 10f, UnidadMedida.UNIDADES, "", pastas, LocalDate.now().plusMonths(6));
    Donacion donacion = new Donacion(List.of(fideos), List.of(juan));
    Donacion donacion2 = new Donacion(List.of(fideos), List.of(andres));
    donacion.asignarA(eric);
    donacion.listaParaEntregar();
    donacion.enCamino();
    donacion.entregada();
    Usuario usuario = new Usuario("admin", "admin");
    usuario.otorgarPermiso(Permiso.ADMINISTRADOR);

    try {
      beginTransaction();
      repoBeneficiarios.guardar(eric);
      repoDonantes.guardar(juan);
      repoDonantes.guardar(andres);
      repoCategorias.guardar(alimentos);
      repoSubcategorias.guardar(lacteos);
      repoSubcategorias.guardar(pastas);
      repoDonaciones.guardar(donacion);
      repoDonaciones.guardar(donacion2);
      repoUsuarios.guardar(usuario);
      commitTransaction();
      System.out.println("Datos iniciales cargados con éxito.");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar los datos iniciales. Se canceló la transacción.");
      e.printStackTrace();
    }

    cargarDonantePrueba(eric, lacteos, pastas);
  }

  // Usuario de prueba con rol donante (login: donante / donante), igual que el admin (admin / admin).
  // Sirve para probar "Mis donaciones" (/donante/donaciones) y "Entidades beneficiarias" (/donante/beneficiarios).
  private void cargarDonantePrueba(Beneficiario eric, Subcategoria lacteos, Subcategoria frutas) {
    DonanteRepository repoDonantes = DonanteRepository.getInstancia();
    DonacionRepository repoDonaciones = DonacionRepository.getInstancia();

    Categoria ropa = new Categoria("ropa");
    Subcategoria abrigos = new Subcategoria("abrigos", ropa);

    Usuario usuarioDonante = new Usuario("donante", "donante");
    usuarioDonante.otorgarPermiso(Permiso.REALIZAR_DONACION);
    Humana juana = new Humana("Juana", "Perez", new Documento(TipoDocumento.DNI, "30111222"),
        LocalDate.of(1990, 5, 10), Genero.FEMENINO, "Calle Falsa 123",
        List.of(new CorreoDeContacto("juana@gmail.com", true)));
    juana.vincularUsuario(usuarioDonante);

    Donacion leche = new Donacion(List.of(
        new Perecedero("Leche entera", 10f, UnidadMedida.LITROS, null, lacteos, LocalDate.now().plusDays(20))),
        List.of(juana));                                   // EN_DEPOSITO
    Donacion manzanas = new Donacion(List.of(
        new Perecedero("Manzanas", 5f, UnidadMedida.KILOGRAMOS, null, frutas, LocalDate.now().plusDays(7))),
        List.of(juana));
    manzanas.asignarA(eric);                               // ASIGNACION_REALIZADA
    Donacion camperas = new Donacion(List.of(
        new NoPerecedero("Camperas", 3f, UnidadMedida.UNIDADES, null, abrigos, true)),
        List.of(juana));
    camperas.asignarA(eric);
    camperas.listaParaEntregar();                          // LISTA_PARA_ENTREGAR

    Donacion frazadas = new Donacion(List.of(
        new NoPerecedero("Frazadas", 8f, UnidadMedida.UNIDADES, null, abrigos, true)),
        List.of(juana));
    frazadas.asignarA(eric);
    frazadas.listaParaEntregar();
    frazadas.enCamino();                                   // EN_TRASLADO

    // para /donante/beneficiarios: una necesidad para eric y una segunda entidad sin necesidades
    eric.agregarNecesidad(Necesidad.crearNecesidadExtraordinaria("Leche para el comedor", lacteos, UnidadMedida.LITROS, 50));
    eric.agregarNecesidad(Necesidad.crearNecesidadRecurrente("Abrigos para invierno", abrigos, UnidadMedida.UNIDADES, 20, Frecuencia.values()[0]));
    Beneficiario comedor = new Beneficiario("Comedor Los Pibes", "Av. Rivadavia 1000",
        List.of(new CorreoDeContacto("comedor@gmail.com", true)));

    try {
      beginTransaction();
      CategoriaRepository.getInstancia().guardar(ropa);
      SubcategoriaRepository.getInstancia().guardar(abrigos);
      UsuarioRepository.getInstancia().guardar(usuarioDonante);
      repoDonantes.guardar(juana);
      repoDonaciones.guardar(leche);
      repoDonaciones.guardar(manzanas);
      repoDonaciones.guardar(camperas);
      repoDonaciones.guardar(frazadas);
      BeneficiarioRepository.getInstancia().actualizar(eric);
      BeneficiarioRepository.getInstancia().guardar(comedor);
      commitTransaction();
      System.out.println("Datos de prueba del donante cargados (donante / donante).");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar los datos de prueba del donante.");
      e.printStackTrace();
    }
  }

}
