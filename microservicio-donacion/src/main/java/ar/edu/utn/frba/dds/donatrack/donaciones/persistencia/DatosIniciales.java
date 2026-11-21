package ar.edu.utn.frba.dds.donatrack.donaciones.persistencia;

import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.beneficiario.Beneficiario;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Bien;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Categoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.Subcategoria;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.UnidadMedida;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.NoPerecedero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.bien.tipobien.Perecedero;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.Donacion;
import ar.edu.utn.frba.dds.donatrack.donaciones.dominio.donacion.EstadoDonacion;
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
import java.time.LocalDateTime;
import java.util.List;

public class DatosIniciales implements WithSimplePersistenceUnit {
  private BeneficiarioRepository repoBeneficiarios = BeneficiarioRepository.getInstancia();
  private DonanteRepository repoDonantes = DonanteRepository.getInstancia();
  private CategoriaRepository repoCategorias = CategoriaRepository.getInstancia();
  private SubcategoriaRepository repoSubcategorias = SubcategoriaRepository.getInstancia();
  private DonacionRepository repoDonaciones = DonacionRepository.getInstancia();
  private RankingRepository repoRankings = RankingRepository.getInstancia();

  public static void init() {
    new DatosIniciales().comenzar();
  }

  public void comenzar() {
    if (!repoCategorias.buscarTodos().isEmpty()) return;

    // Beneficiarios (con coordenadas para que aparezcan en Seguimiento y Mapa)
    Beneficiario comedorAmerica = new Beneficiario(
        "Comedor america",
        "Av. Rivadavia 10000, CABA",
        "-34.6373",
        "-58.5048",
        List.of(new CorreoDeContacto("comedorAmerica@gmail.com", true))
    );

    Beneficiario comedorPibes = new Beneficiario(
        "Comedor Los Pibes",
        "Av. Rivadavia 4500, CABA",
        "-34.6186",
        "-58.4310",
        List.of(new CorreoDeContacto("comedor@gmail.com", true))
    );

    Beneficiario hogarSantaRita = new Beneficiario(
        "Hogar Santa Rita",
        "Av. Santa Fe 3200, CABA",
        "-34.5889",
        "-58.4105",
        List.of(new CorreoDeContacto("santarita@gmail.com", true))
    );

    Beneficiario merenderoRayito = new Beneficiario(
        "Merendero Rayito de Sol",
        "Av. Cabildo 2100",
        List.of(new CorreoDeContacto("rayitodesol@gmail.com", true))
    );
    Beneficiario fundacionAbrazo = new Beneficiario(
        "Fundación Abrazo",
        "Av. Almirante Brown 1200",
        List.of(new CorreoDeContacto("abrazo@gmail.com", true))
    );
    Beneficiario comedorEsperanza = new Beneficiario(
        "Comedor Nueva Esperanza",
        "Av. Nazca 1800",
        List.of(new CorreoDeContacto("esperanza@gmail.com", true))
    );
    Beneficiario centroComunitario = new Beneficiario(
        "Centro Comunitario El Ceibo",
        "Av. Triunvirato 4500",
        List.of(new CorreoDeContacto("elceibo@gmail.com", true))
    );

    // Donantes
    Humana juan = new Humana("Juan", "Perez", new Documento(TipoDocumento.DNI, "12345678"), LocalDate.now().minusYears(25), Genero.MASCULINO, "siempre viva 555", List.of(new CorreoDeContacto("correoJuan@gmail.com", true)));
    Humana juana = new Humana("Juana", "Perez", new Documento(TipoDocumento.DNI, "30111222"), LocalDate.of(1990, 5, 10), Genero.FEMENINO, "Calle Falsa 123", List.of(new CorreoDeContacto("juana@gmail.com", true)));

    // Categorías
    Categoria alimentos = new Categoria("Alimentos");
    Categoria ropa = new Categoria("Vestimenta");
    Categoria mobiliario = new Categoria("Mobiliario");
    Categoria ropaDeCama = new Categoria("Ropa de cama");
    Categoria higiene = new Categoria("Higiene personal");
    Categoria limpieza = new Categoria("Limpieza");

    // Subcategorías
    Subcategoria pastas = new Subcategoria("Fideos secos", alimentos);
    Subcategoria arroz = new Subcategoria("Arroz", alimentos);
    Subcategoria legumbres = new Subcategoria("Legumbres secas", alimentos);
    Subcategoria aceite = new Subcategoria("Aceite vegetal", alimentos);
    Subcategoria lacteos = new Subcategoria("Leche", alimentos);
    Subcategoria frutas = new Subcategoria("Frutas", alimentos);
    Subcategoria verduras = new Subcategoria("Verduras", alimentos);
    Subcategoria conservas = new Subcategoria("Conservas", alimentos);

    Subcategoria abrigos = new Subcategoria("Camperas de abrigo", ropa);
    Subcategoria remeras = new Subcategoria("Remeras", ropa);
    Subcategoria pantalones = new Subcategoria("Pantalones", ropa);
    Subcategoria ropaInfantil = new Subcategoria("Ropa infantil", ropa);
    Subcategoria calzado = new Subcategoria("Calzado", ropa);

    Subcategoria sillas = new Subcategoria("Sillas", mobiliario);
    Subcategoria mesas = new Subcategoria("Mesas", mobiliario);
    Subcategoria bancos = new Subcategoria("Bancos escolares", mobiliario);
    Subcategoria escritorios = new Subcategoria("Escritorios", mobiliario);
    Subcategoria camas = new Subcategoria("Camas", mobiliario);
    Subcategoria colchones = new Subcategoria("Colchones", mobiliario);

    Subcategoria frazadas = new Subcategoria("Frazadas", ropaDeCama);
    Subcategoria sabanas = new Subcategoria("Sábanas", ropaDeCama);
    Subcategoria almohadas = new Subcategoria("Almohadas", ropaDeCama);

    Subcategoria jabon = new Subcategoria("Jabón", higiene);
    Subcategoria paniales = new Subcategoria("Pañales", higiene);
    Subcategoria pastaDental = new Subcategoria("Pasta dental", higiene);
    Subcategoria shampoo = new Subcategoria("Shampoo", higiene);

    Subcategoria lavandina = new Subcategoria("Lavandina", limpieza);
    Subcategoria detergente = new Subcategoria("Detergente", limpieza);
    Subcategoria desinfectantes = new Subcategoria("Desinfectantes", limpieza);

    // Bienes base (1 a 15)
    Bien fideo = new Perecedero("Fideos secos", 10f, UnidadMedida.UNIDADES, null, pastas, LocalDate.now().plusMonths(6));
    Bien leche = new Perecedero("Leche entera", 10f, UnidadMedida.LITROS, null, lacteos, LocalDate.now().plusDays(20));
    Bien manzana = new Perecedero("Manzanas", 5f, UnidadMedida.KILOGRAMOS, null, frutas, LocalDate.now().plusDays(7));
    Bien campera = new NoPerecedero("Camperas", 3f, UnidadMedida.UNIDADES, "/comun/img/entregas/campera.jpg", abrigos, true);
    Bien campera2 = new NoPerecedero("Camperas", 3f, UnidadMedida.UNIDADES, "/comun/img/entregas/campera.jpg", abrigos, true);
    Bien frazada = new NoPerecedero("Frazadas", 8f, UnidadMedida.UNIDADES, null, frazadas, true);
    Bien frazada2 = new NoPerecedero("Frazadas", 8f, UnidadMedida.UNIDADES, null, frazadas, true);
    Bien arrozBien = new Perecedero("Arroz largo fino", 15f, UnidadMedida.KILOGRAMOS, null, arroz, LocalDate.now().plusMonths(8));
    Bien aceiteBien = new Perecedero("Aceite de girasol", 12f, UnidadMedida.LITROS, "/comun/img/entregas/aceite.jpg", aceite, LocalDate.now().plusMonths(10));
    Bien yogurPrueba = new Perecedero("Yogur bebible", 12f, UnidadMedida.LITROS, null, lacteos, LocalDate.now().plusDays(3));

    Bien lecheHist = new Perecedero("Leche en polvo fortificada", 25f, UnidadMedida.KILOGRAMOS, null, lacteos, LocalDate.now().plusMonths(5));
    Bien panialesHist = new NoPerecedero("Pañales descartables talle G", 40f, UnidadMedida.UNIDADES, null, paniales, true);
    Bien frazadasHist = new NoPerecedero("Frazadas polares", 18f, UnidadMedida.UNIDADES, null, frazadas, true);
    Bien legumbresHist = new Perecedero("Lentejas secas", 30f, UnidadMedida.KILOGRAMOS, "/comun/img/entregas/lentejas.jpg", legumbres, LocalDate.now().plusMonths(9));
    Bien limpiezaHist = new NoPerecedero("Bidones de lavandina", 15f, UnidadMedida.LITROS, null, lavandina, true);
    Bien sillasHist = new NoPerecedero("Sillas plásticas reforzadas", 12f, UnidadMedida.UNIDADES, "/comun/img/entregas/sillas.jpg", sillas, true);
    Bien fideoHist = new Perecedero("Fideos tirabuzón", 20f, UnidadMedida.UNIDADES, "/comun/img/entregas/fideos.jpg", pastas, LocalDate.now().plusMonths(6));
    Bien aceiteHist = new Perecedero("Aceite mezcla", 10f, UnidadMedida.LITROS, null, aceite, LocalDate.now().plusMonths(10));

    // Bienes adicionales para múltiples camiones y entregas en traslado (16 a 20)
    Bien frazadasTraslado = new NoPerecedero("Frazadas térmicas", 10f, UnidadMedida.UNIDADES, null, frazadas, true);
    Bien sabanasTraslado = new NoPerecedero("Sábanas 1 plaza", 12f, UnidadMedida.UNIDADES, null, sabanas, true);
    Bien lecheTraslado = new Perecedero("Leche entera larga vida", 24f, UnidadMedida.LITROS, null, lacteos, LocalDate.now().plusMonths(4));
    Bien aceiteTraslado = new Perecedero("Aceite de maíz", 10f, UnidadMedida.LITROS, null, aceite, LocalDate.now().plusMonths(8));
    Bien camperasTraslado = new NoPerecedero("Camperas impermeables", 6f, UnidadMedida.UNIDADES, null, abrigos, true);
    Bien panialesTraslado = new NoPerecedero("Pañales infantiles", 50f, UnidadMedida.UNIDADES, null, paniales, true);
    Bien lavandinaTraslado = new NoPerecedero("Lavandina concentrada", 20f, UnidadMedida.LITROS, null, lavandina, true);
    Bien detergenteTraslado = new NoPerecedero("Detergente biodegradable", 15f, UnidadMedida.LITROS, null, detergente, true);

    // Donaciones 1..15
    Donacion donacion1 = new Donacion(List.of(fideo), List.of(juan));
    Donacion donacion2 = new Donacion(List.of(leche), List.of(juan));
    Donacion donacion3 = new Donacion(List.of(manzana), List.of(juana)); // ID 3 -> EN_TRASLADO (Juana -> Comedor America, Camión 1)
    Donacion donacion4 = new Donacion(List.of(campera), List.of(juana)); // ID 4 -> ENTREGADA
    Donacion donacion5 = new Donacion(List.of(frazada), List.of(juana)); // ID 5 -> Ranking
    Donacion donacion6 = new Donacion(List.of(frazada2, campera2), List.of(juan, juana)); // ID 6 -> Ranking
    Donacion donacion7 = new Donacion(List.of(arrozBien), List.of(juan)); // ID 7 -> EN_TRASLADO (Juan -> Comedor Los Pibes, Camión 2)
    Donacion donacion8 = new Donacion(List.of(aceiteBien), List.of(juan)); // ID 8 -> ENTREGADA

    Donacion donacion9 = new Donacion(List.of(lecheHist, panialesHist), List.of(juan));
    Donacion donacion10 = new Donacion(List.of(frazadasHist), List.of(juana));
    Donacion donacion11 = new Donacion(List.of(legumbresHist), List.of(juan, juana));
    Donacion donacion12 = new Donacion(List.of(limpiezaHist), List.of(juana));
    Donacion donacion13 = new Donacion(List.of(sillasHist), List.of(juan));
    Donacion donacion14 = new Donacion(List.of(fideoHist, aceiteHist), List.of(juana));
    Donacion donacion15 = new Donacion(List.of(yogurPrueba), List.of(juan)); // ID 15 -> EN_DEPOSITO

    // Donaciones 16..20 (nuevas en traslado repartidas entre camiones y donantes)
    Donacion donacion16 = new Donacion(List.of(frazadasTraslado, sabanasTraslado), List.of(juan)); // ID 16 -> EN_TRASLADO (Juan -> Comedor America, Camión 1)
    Donacion donacion17 = new Donacion(List.of(lecheTraslado, aceiteTraslado), List.of(juana));   // ID 17 -> EN_TRASLADO (Juana -> Comedor Los Pibes, Camión 2)
    Donacion donacion18 = new Donacion(List.of(camperasTraslado), List.of(juan));                  // ID 18 -> EN_TRASLADO (Juan -> Comedor America, Camión 3)
    Donacion donacion19 = new Donacion(List.of(panialesTraslado), List.of(juana));                 // ID 19 -> EN_TRASLADO (Juana -> Hogar Santa Rita, Camión 4)
    Donacion donacion20 = new Donacion(List.of(lavandinaTraslado, detergenteTraslado), List.of(juan)); // ID 20 -> EN_TRASLADO (Juan -> Hogar Santa Rita, Camión 4)

    // Rankings
    Ranking ranking1 = new Ranking(donacion5, List.of(comedorAmerica, comedorPibes));
    Ranking ranking2 = new Ranking(donacion6, List.of(comedorAmerica, comedorPibes));

    // Estados y ciclos de vida
    donacion1.asignarA(comedorAmerica);

    donacion2.asignarA(comedorAmerica);
    donacion2.listaParaEntregar();

    ponerEnTraslado(donacion3, comedorAmerica);
    completarEntregaHistorica(donacion4, comedorAmerica, LocalDateTime.now().minusDays(2));
    ponerEnTraslado(donacion7, comedorPibes);
    completarEntregaHistorica(donacion8, comedorPibes, LocalDateTime.now().minusDays(1));

    completarEntregaHistorica(donacion9, hogarSantaRita, LocalDateTime.of(2026, 10, 12, 14, 30));
    completarEntregaHistorica(donacion10, merenderoRayito, LocalDateTime.of(2026, 10, 18, 11, 15));
    completarEntregaHistorica(donacion11, fundacionAbrazo, LocalDateTime.of(2026, 10, 25, 16, 45));
    completarEntregaHistorica(donacion12, comedorEsperanza, LocalDateTime.of(2026, 11, 2, 10, 20));
    completarEntregaHistorica(donacion13, centroComunitario, LocalDateTime.of(2026, 11, 8, 15, 10));
    completarEntregaHistorica(donacion14, hogarSantaRita, LocalDateTime.of(2026, 11, 14, 17, 0));

    ponerEnTraslado(donacion16, comedorAmerica);
    ponerEnTraslado(donacion17, comedorPibes);
    ponerEnTraslado(donacion18, comedorAmerica);
    ponerEnTraslado(donacion19, hogarSantaRita);
    ponerEnTraslado(donacion20, hogarSantaRita);

    // Necesidades
    Necesidad necesidadLecheRecurrente = Necesidad.crearNecesidadRecurrente("Leche para el comedor", lacteos, UnidadMedida.LITROS, 50, Frecuencia.MENSUAL);
    Necesidad necesidadAbrigo = Necesidad.crearNecesidadExtraordinaria("Abrigos para invierno", abrigos, UnidadMedida.UNIDADES, 20);
    comedorAmerica.agregarNecesidad(necesidadLecheRecurrente);
    comedorAmerica.agregarNecesidad(necesidadAbrigo);

    try {
      beginTransaction();
      repoBeneficiarios.guardar(comedorAmerica);
      repoBeneficiarios.guardar(comedorPibes);
      repoBeneficiarios.guardar(hogarSantaRita);
      repoBeneficiarios.guardar(merenderoRayito);
      repoBeneficiarios.guardar(fundacionAbrazo);
      repoBeneficiarios.guardar(comedorEsperanza);
      repoBeneficiarios.guardar(centroComunitario);

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

      // Guardamos en orden estricto 1L..20L antes de Rankings
      repoDonaciones.guardar(donacion1);  // ID 1L
      repoDonaciones.guardar(donacion2);  // ID 2L
      repoDonaciones.guardar(donacion3);  // ID 3L -> EN_TRASLADO
      repoDonaciones.guardar(donacion4);  // ID 4L
      repoDonaciones.guardar(donacion5);  // ID 5L
      repoDonaciones.guardar(donacion6);  // ID 6L
      repoDonaciones.guardar(donacion7);  // ID 7L -> EN_TRASLADO
      repoDonaciones.guardar(donacion8);  // ID 8L
      repoDonaciones.guardar(donacion9);  // ID 9L
      repoDonaciones.guardar(donacion10); // ID 10L
      repoDonaciones.guardar(donacion11); // ID 11L
      repoDonaciones.guardar(donacion12); // ID 12L
      repoDonaciones.guardar(donacion13); // ID 13L
      repoDonaciones.guardar(donacion14); // ID 14L
      repoDonaciones.guardar(donacion15); // ID 15L
      repoDonaciones.guardar(donacion16); // ID 16L -> EN_TRASLADO
      repoDonaciones.guardar(donacion17); // ID 17L -> EN_TRASLADO
      repoDonaciones.guardar(donacion18); // ID 18L -> EN_TRASLADO
      repoDonaciones.guardar(donacion19); // ID 19L -> EN_TRASLADO
      repoDonaciones.guardar(donacion20); // ID 20L -> EN_TRASLADO

      repoRankings.guardar(ranking1);
      repoRankings.guardar(ranking2);

      commitTransaction();
      System.out.println("Datos iniciales de Donaciones cargados con éxito.");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar los datos iniciales. Se canceló la transacción.");
      e.printStackTrace();
    }
  }

  private void ponerEnTraslado(Donacion donacion, Beneficiario destino) {
    donacion.asignarA(destino);
    donacion.listaParaEntregar();
    donacion.enCamino();
  }

  private void completarEntregaHistorica(Donacion donacion, Beneficiario destino, LocalDateTime fechaEntrega) {
    donacion.asignarA(destino);
    donacion.listaParaEntregar();
    donacion.enCamino();
    donacion.entregada();

    List<EstadoDonacion> historial = donacion.getHistorialEstados();
    if (historial != null && !historial.isEmpty()) {
      EstadoDonacion ultimoEstado = historial.get(historial.size() - 1);
      ultimoEstado.setFecha(fechaEntrega);
    }
  }
}
