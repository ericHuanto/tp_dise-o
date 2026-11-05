package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Categoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.CorreoDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import java.util.List;

public class DatosIniciales implements WithSimplePersistenceUnit {

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
    CategoriaRepository repoCategorias = CategoriaRepository.getInstancia();
    SubcategoriaRepository repoSubcategorias = SubcategoriaRepository.getInstancia();
    UsuarioRepository repoUsuarios = UsuarioRepository.getInstancia();

    Beneficiario eric = new Beneficiario("ericH", "siempre viva, Springfield", List.of(new CorreoDeContacto("correo@gmail.com", true)));
    Categoria alimentos = new Categoria("alimentos");
    Subcategoria lacteos = new Subcategoria("lacteos", alimentos);
    Subcategoria frutas = new Subcategoria("frutas", alimentos);
    Usuario usuario = new Usuario("admin", "admin");
    usuario.otorgarPermiso(Permiso.ADMINISTRADOR);

    try {
      beginTransaction();
      repoBeneficiarios.guardar(eric);
      repoCategorias.guardar(alimentos);
      repoSubcategorias.guardar(lacteos);
      repoSubcategorias.guardar(frutas);
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
