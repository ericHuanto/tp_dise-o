package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Categoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.Perecedero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.Documento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.documento.TipoDocumento;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.Genero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donante.tipodonantes.persona.Humana;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.CorreoDeContacto;
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

  }

}
