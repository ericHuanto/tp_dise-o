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
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.generadorrankings.Ranking;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.mediocontacto.CorreoDeContacto;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Frecuencia;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Permiso;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.usuario.Usuario;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.LocalDate;
import java.util.List;

public class DatosIniciales implements WithSimplePersistenceUnit {
  private BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
  private DonanteRepository repoDonantes = DonanteRepository.getInstancia();
  private CategoriaRepository repoCategorias = CategoriaRepository.getInstancia();
  private SubcategoriaRepository repoSubcategorias = SubcategoriaRepository.getInstancia();
  private DonacionRepository repoDonaciones = DonacionRepository.getInstancia();
  private UsuarioRepository repoUsuarios = UsuarioRepository.getInstancia();
  private RankingRepository repoRankings = RankingRepository.getInstancia();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    //Usuarios
    Usuario usuarioBeneficiario = new Usuario("comedorA", "beneficiario");
    Usuario usuarioBeneficiario2 = new Usuario("comedorP", "beneficiario");
    Usuario usuarioDonante = new Usuario("juan", "donante");
    Usuario usuarioDonante2 = new Usuario("juana", "donante");
    Usuario admin = new Usuario("admin", "admin");
    //Beneficiarios
    Beneficiario comedorAmerica = new Beneficiario("Comedor america", "Av. Rivadavia 10000", List.of(new CorreoDeContacto("comedorAmerica@gmail.com", true)));
    Beneficiario comedorPibes = new Beneficiario("Comedor Los Pibes", "Av. Rivadavia 1000", List.of(new CorreoDeContacto("comedor@gmail.com", true)));
    //Donantes
    Humana juan = new Humana("Juan", "Perez", new Documento(TipoDocumento.DNI, "12345678"), LocalDate.now().minusYears(25), Genero.MASCULINO, "siempre viva 555", List.of(new CorreoDeContacto("correoJuan@gmail.com", true)));
    Humana juana = new Humana("Juana", "Perez", new Documento(TipoDocumento.DNI, "30111222"), LocalDate.of(1990, 5, 10), Genero.FEMENINO, "Calle Falsa 123", List.of(new CorreoDeContacto("juana@gmail.com", true)));
    //Categorias
    Categoria alimentos = new Categoria("Alimentos");
    Categoria ropa = new Categoria("Vestimenta");
    Categoria mobiliario = new Categoria("Mobiliario");
    Categoria ropaDeCama = new Categoria("Ropa de cama");
    Categoria higiene = new Categoria("Higiene personal");
    Categoria limpieza = new Categoria("Limpieza");
    // Subcategorías de alimentos
    Subcategoria pastas = new Subcategoria("Fideos secos", alimentos);
    Subcategoria arroz = new Subcategoria("Arroz", alimentos);
    Subcategoria legumbres = new Subcategoria("Legumbres secas", alimentos);
    Subcategoria aceite = new Subcategoria("Aceite vegetal", alimentos);
    Subcategoria lacteos = new Subcategoria("Leche", alimentos);
    Subcategoria frutas = new Subcategoria("Frutas", alimentos);
    Subcategoria verduras = new Subcategoria("Verduras", alimentos);
    Subcategoria conservas = new Subcategoria("Conservas", alimentos);
    // Subcategorías de vestimenta
    Subcategoria abrigos = new Subcategoria("Camperas de abrigo", ropa);
    Subcategoria remeras = new Subcategoria("Remeras", ropa);
    Subcategoria pantalones = new Subcategoria("Pantalones", ropa);
    Subcategoria ropaInfantil = new Subcategoria("Ropa infantil", ropa);
    Subcategoria calzado = new Subcategoria("Calzado", ropa);
    // Subcategorías de mobiliario
    Subcategoria sillas = new Subcategoria("Sillas", mobiliario);
    Subcategoria mesas = new Subcategoria("Mesas", mobiliario);
    Subcategoria bancos = new Subcategoria("Bancos escolares", mobiliario);
    Subcategoria escritorios = new Subcategoria("Escritorios", mobiliario);
    Subcategoria camas = new Subcategoria("Camas", mobiliario);
    Subcategoria colchones = new Subcategoria("Colchones", mobiliario);
    // Subcategorías de ropa de cama
    Subcategoria frazadas = new Subcategoria("Frazadas", ropaDeCama);
    Subcategoria sabanas = new Subcategoria("Sábanas", ropaDeCama);
    Subcategoria almohadas = new Subcategoria("Almohadas", ropaDeCama);
    // Subcategorías de higiene personal
    Subcategoria jabon = new Subcategoria("Jabón", higiene);
    Subcategoria paniales = new Subcategoria("Pañales", higiene);
    Subcategoria pastaDental = new Subcategoria("Pasta dental", higiene);
    Subcategoria shampoo = new Subcategoria("Shampoo", higiene);

    // Subcategorías de limpieza
    Subcategoria lavandina = new Subcategoria("Lavandina", limpieza);
    Subcategoria detergente = new Subcategoria("Detergente", limpieza);
    Subcategoria desinfectantes = new Subcategoria("Desinfectantes", limpieza);


    // Bienes
    Bien fideo = new Perecedero("Fideos secos", 10f, UnidadMedida.UNIDADES, null, pastas, LocalDate.now().plusMonths(6));

    Bien leche = new Perecedero("Leche entera", 10f, UnidadMedida.LITROS, null, lacteos, LocalDate.now().plusDays(20));

    Bien manzana = new Perecedero(
        "Manzanas", 5f, UnidadMedida.KILOGRAMOS, null, frutas, LocalDate.now().plusDays(7));

    Bien campera = new NoPerecedero(
        "Camperas", 3f, UnidadMedida.UNIDADES, null, abrigos, true);

    Bien campera2 = new NoPerecedero("Camperas", 3f, UnidadMedida.UNIDADES,null, abrigos, true);

    Bien frazada = new NoPerecedero("Frazadas", 8f, UnidadMedida.UNIDADES, null, frazadas, true);

    Bien frazada2 = new NoPerecedero("Frazadas", 8f, UnidadMedida.UNIDADES, null, frazadas, true);

    // Necesidades

    Necesidad necesidadLecheRecurrente = Necesidad.crearNecesidadRecurrente("Leche para el comedor", lacteos, UnidadMedida.LITROS, 50, Frecuencia.MENSUAL);
    Necesidad necesidadAbrigo = Necesidad.crearNecesidadExtraordinaria("Abrigos para invierno", abrigos, UnidadMedida.UNIDADES, 20);
    //Donaciones
    Donacion donacion1 = new Donacion(List.of(fideo), List.of(juan));
    Donacion donacion2 = new Donacion(List.of(leche), List.of(juan));
    Donacion donacion3 = new Donacion(List.of(manzana), List.of(juana));
    Donacion donacion4 = new Donacion(List.of(campera), List.of(juana));
    Donacion donacion5 = new Donacion(List.of(frazada), List.of(juana));
    Donacion donacion6 = new Donacion(List.of(frazada2, campera2), List.of(juan, juana));
    //Rankings
    Ranking ranking1 = new Ranking(donacion5, List.of(comedorAmerica, comedorPibes));
    Ranking ranking2 = new Ranking(donacion6, List.of(comedorAmerica, comedorPibes));

    //Acciones
    donacion1.asignarA(comedorAmerica);
    donacion2.asignarA(comedorAmerica);
    donacion2.listaParaEntregar();
    donacion3.asignarA(comedorAmerica);
    donacion3.listaParaEntregar();
    donacion3.enCamino();
    donacion4.asignarA(comedorAmerica);
    donacion4.listaParaEntregar();
    donacion4.enCamino();
    donacion4.entregada();
    comedorAmerica.agregarNecesidad(necesidadLecheRecurrente);
    comedorAmerica.agregarNecesidad(necesidadAbrigo);
    usuarioBeneficiario.otorgarPermiso(Permiso.SOLICITAR_AYUDA);
    usuarioBeneficiario2.otorgarPermiso(Permiso.SOLICITAR_AYUDA);
    usuarioDonante.otorgarPermiso(Permiso.REALIZAR_DONACION);
    usuarioDonante.otorgarPermiso(Permiso.REALIZAR_DONACION);
    comedorAmerica.vincularUsuario(usuarioBeneficiario);
    comedorPibes.vincularUsuario(usuarioBeneficiario2);
    juan.vincularUsuario(usuarioDonante);
    juana.vincularUsuario(usuarioDonante2);
    admin.otorgarPermiso(Permiso.ADMINISTRADOR);

    //Transacción
    try {
      beginTransaction();
      repoUsuarios.guardar(admin);
      repoUsuarios.guardar(usuarioDonante);
      repoUsuarios.guardar(usuarioDonante2);
      repoUsuarios.guardar(usuarioBeneficiario);
      repoUsuarios.guardar(usuarioBeneficiario2);
      repoBeneficiarios.guardar(comedorAmerica);
      repoBeneficiarios.guardar(comedorPibes);
      repoDonantes.guardar(juan);
      repoDonantes.guardar(juana);
      repoCategorias.guardar(ropa);
      repoCategorias.guardar(alimentos);
      repoCategorias.guardar(ropa);
      repoCategorias.guardar(mobiliario);
      repoCategorias.guardar(ropaDeCama);
      repoCategorias.guardar(higiene);
      repoCategorias.guardar(limpieza);
      repoSubcategorias.guardar(pastas);
      repoSubcategorias.guardar(arroz);
      repoSubcategorias.guardar(legumbres);
      repoSubcategorias.guardar(aceite);
      repoSubcategorias.guardar(lacteos);
      repoSubcategorias.guardar(frutas);
      repoSubcategorias.guardar(verduras);
      repoSubcategorias.guardar(conservas);
      repoSubcategorias.guardar(abrigos);
      repoSubcategorias.guardar(remeras);
      repoSubcategorias.guardar(pantalones);
      repoSubcategorias.guardar(ropaInfantil);
      repoSubcategorias.guardar(calzado);
      repoSubcategorias.guardar(sillas);
      repoSubcategorias.guardar(mesas);
      repoSubcategorias.guardar(bancos);
      repoSubcategorias.guardar(escritorios);
      repoSubcategorias.guardar(camas);
      repoSubcategorias.guardar(colchones);
      repoSubcategorias.guardar(frazadas);
      repoSubcategorias.guardar(sabanas);
      repoSubcategorias.guardar(almohadas);
      repoSubcategorias.guardar(jabon);
      repoSubcategorias.guardar(paniales);
      repoSubcategorias.guardar(pastaDental);
      repoSubcategorias.guardar(shampoo);
      repoSubcategorias.guardar(lavandina);
      repoSubcategorias.guardar(detergente);
      repoSubcategorias.guardar(desinfectantes);
      repoDonaciones.guardar(donacion1);
      repoDonaciones.guardar(donacion2);
      repoDonaciones.guardar(donacion3);
      repoDonaciones.guardar(donacion4);
      repoDonaciones.guardar(donacion5);
      repoDonaciones.guardar(donacion6);
      repoRankings.guardar(ranking1);
      repoRankings.guardar(ranking2);
      commitTransaction();
      System.out.println("Datos iniciales cargados con éxito.");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar los datos iniciales. Se canceló la transacción.");
      e.printStackTrace();
    }

  }

}
