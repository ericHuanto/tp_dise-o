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
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.LocalDate;
import java.util.List;

public class DatosIniciales implements WithSimplePersistenceUnit {
  private final BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
  private final DonanteRepository repoDonantes = DonanteRepository.getInstancia();
  private final CategoriaRepository repoCategorias = CategoriaRepository.getInstancia();
  private final SubcategoriaRepository repoSubcategorias = SubcategoriaRepository.getInstancia();
  private final DonacionRepository repoDonaciones = DonacionRepository.getInstancia();
  private final RankingRepository repoRankings = RankingRepository.getInstancia();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    // Si la db no está vacía, no los inserta nuevamente
    if (!repoCategorias.buscarTodos().isEmpty()) return;

    // 1. Beneficiarios
    Beneficiario comedorAmerica = new Beneficiario("Comedor america", "Av. Rivadavia 10000", List.of(new CorreoDeContacto("comedorAmerica@gmail.com", true)));
    Beneficiario comedorPibes = new Beneficiario("Comedor Los Pibes", "Av. Rivadavia 1000", List.of(new CorreoDeContacto("comedor@gmail.com", true)));

    // 2. Donantes
    Humana juan = new Humana("Juan", "Perez", new Documento(TipoDocumento.DNI, "12345678"), LocalDate.now().minusYears(25), Genero.MASCULINO, "siempre viva 555", List.of(new CorreoDeContacto("correoJuan@gmail.com", true)));
    Humana juana = new Humana("Juana", "Perez", new Documento(TipoDocumento.DNI, "30111222"), LocalDate.of(1990, 5, 10), Genero.FEMENINO, "Calle Falsa 123", List.of(new CorreoDeContacto("juana@gmail.com", true)));

    // 3. Categorías
    Categoria alimentos = new Categoria("Alimentos");
    Categoria ropa = new Categoria("Vestimenta");
    Categoria mobiliario = new Categoria("Mobiliario");
    Categoria ropaDeCama = new Categoria("Ropa de cama");
    Categoria higiene = new Categoria("Higiene personal");
    Categoria limpieza = new Categoria("Limpieza");

    // 4. Subcategorías
    // - Alimentos
    Subcategoria pastas = new Subcategoria("Fideos secos", alimentos);
    Subcategoria arroz = new Subcategoria("Arroz", alimentos);
    Subcategoria legumbres = new Subcategoria("Legumbres secas", alimentos);
    Subcategoria aceite = new Subcategoria("Aceite vegetal", alimentos);
    Subcategoria lacteos = new Subcategoria("Leche", alimentos);
    Subcategoria frutas = new Subcategoria("Frutas", alimentos);
    Subcategoria verduras = new Subcategoria("Verduras", alimentos);
    Subcategoria conservas = new Subcategoria("Conservas", alimentos);
    // - Vestimenta
    Subcategoria abrigos = new Subcategoria("Camperas de abrigo", ropa);
    Subcategoria remeras = new Subcategoria("Remeras", ropa);
    Subcategoria pantalones = new Subcategoria("Pantalones", ropa);
    Subcategoria ropaInfantil = new Subcategoria("Ropa infantil", ropa);
    Subcategoria calzado = new Subcategoria("Calzado", ropa);
    // - Mobiliario
    Subcategoria sillas = new Subcategoria("Sillas", mobiliario);
    Subcategoria mesas = new Subcategoria("Mesas", mobiliario);
    Subcategoria bancos = new Subcategoria("Bancos escolares", mobiliario);
    Subcategoria escritorios = new Subcategoria("Escritorios", mobiliario);
    Subcategoria camas = new Subcategoria("Camas", mobiliario);
    Subcategoria colchones = new Subcategoria("Colchones", mobiliario);
    // - Ropa de cama
    Subcategoria frazadas = new Subcategoria("Frazadas", ropaDeCama);
    Subcategoria sabanas = new Subcategoria("Sábanas", ropaDeCama);
    Subcategoria almohadas = new Subcategoria("Almohadas", ropaDeCama);
    // - Higiene personal
    Subcategoria jabon = new Subcategoria("Jabón", higiene);
    Subcategoria paniales = new Subcategoria("Pañales", higiene);
    Subcategoria pastaDental = new Subcategoria("Pasta dental", higiene);
    Subcategoria shampoo = new Subcategoria("Shampoo", higiene);
    // - Limpieza
    Subcategoria lavandina = new Subcategoria("Lavandina", limpieza);
    Subcategoria detergente = new Subcategoria("Detergente", limpieza);
    Subcategoria desinfectantes = new Subcategoria("Desinfectantes", limpieza);

    // 5. Bienes
    Bien fideo = new Perecedero("Fideos secos", 10f, UnidadMedida.UNIDADES, null, pastas, LocalDate.now().plusMonths(6));
    Bien leche = new Perecedero("Leche entera", 10f, UnidadMedida.LITROS, null, lacteos, LocalDate.now().plusDays(20));
    Bien manzana = new Perecedero("Manzanas", 5f, UnidadMedida.KILOGRAMOS, null, frutas, LocalDate.now().plusDays(7));
    Bien campera = new NoPerecedero("Camperas", 3f, UnidadMedida.UNIDADES, null, abrigos, true);
    Bien campera2 = new NoPerecedero("Camperas", 3f, UnidadMedida.UNIDADES,null, abrigos, true);
    Bien frazada = new NoPerecedero("Frazadas", 8f, UnidadMedida.UNIDADES, null, frazadas, true);
    Bien frazada2 = new NoPerecedero("Frazadas", 8f, UnidadMedida.UNIDADES, null, frazadas, true);

    // 6. Necesidades
    Necesidad necesidadLecheRecurrente = Necesidad.crearNecesidadRecurrente("Leche para el comedor", lacteos, UnidadMedida.LITROS, 50, Frecuencia.MENSUAL);
    Necesidad necesidadAbrigo = Necesidad.crearNecesidadExtraordinaria("Abrigos para invierno", abrigos, UnidadMedida.UNIDADES, 20);

    // 7. Donaciones
    Donacion donacion1 = new Donacion(List.of(fideo), List.of(juan));
    Donacion donacion2 = new Donacion(List.of(leche), List.of(juan));
    Donacion donacion3 = new Donacion(List.of(manzana), List.of(juana));
    Donacion donacion4 = new Donacion(List.of(campera), List.of(juana));
    Donacion donacion5 = new Donacion(List.of(frazada), List.of(juana));
    Donacion donacion6 = new Donacion(List.of(frazada2, campera2), List.of(juan, juana));

    // 8. Rankings
    Ranking ranking1 = new Ranking(donacion5, List.of(comedorAmerica, comedorPibes));
    Ranking ranking2 = new Ranking(donacion6, List.of(comedorAmerica, comedorPibes));

    // 9. Acciones y Asignaciones
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

    // 10. Transacción
    try {
      beginTransaction();
      repoBeneficiarios.guardar(comedorAmerica);
      repoBeneficiarios.guardar(comedorPibes);
      repoDonantes.guardar(juan);
      repoDonantes.guardar(juana);
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

      imprimirResumenEnConsola();

    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("❌ Error al cargar los datos iniciales. Se canceló la transacción.");
      e.printStackTrace();
    }
  }

  private void imprimirResumenEnConsola() {
    System.out.println("\n==========================================================================");
    System.out.println(" ✅ DATOS INICIALES MOCKEADOS EN MICROSERVICIO (DONACIONES)");
    System.out.println("==========================================================================");
    System.out.println(" Entidades cargadas:");
    System.out.println("   - 6 Categorías y 24 Subcategorías");
    System.out.println("   - 6 Donaciones en distintos estados");
    System.out.println("   - 2 Rankings");
    System.out.println("   - 2 Necesidades (Recurrente y Extraordinaria)");
    System.out.println("--------------------------------------------------------------------------");
    System.out.printf(" %-15s | %-20s | %-8s%n", "ACTOR", "NOMBRE", "ID ASIGNADO");
    System.out.println("--------------------------------------------------------------------------");
    System.out.printf(" %-15s | %-20s | %-8s%n", "Beneficiario", "Comedor america", "1");
    System.out.printf(" %-15s | %-20s | %-8s%n", "Beneficiario", "Comedor Los Pibes", "2");
    System.out.printf(" %-15s | %-20s | %-8s%n", "Donante", "Juan Perez", "1");
    System.out.printf(" %-15s | %-20s | %-8s%n", "Donante", "Juana Perez", "2");
    System.out.println("==========================================================================\n");
  }
}