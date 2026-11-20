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
    //Si la db no está vacía, no los inserta nuevamente
    if (!repoCategorias.buscarTodos().isEmpty()) return;

    //Beneficiarios
    // Comedor América -> Av. Rivadavia 10000 (Villa Luro)
    Beneficiario comedorAmerica = new Beneficiario(
        "Comedor america",
        "Av. Rivadavia 10000, CABA",
        "-34.6373",
        "-58.5048",
        List.of(new CorreoDeContacto("comedorAmerica@gmail.com", true))
    );

// Comedor Los Pibes -> Av. Rivadavia 4500 (Caballito)
    Beneficiario comedorPibes = new Beneficiario(
        "Comedor Los Pibes",
        "Av. Rivadavia 4500, CABA",
        List.of(new CorreoDeContacto("comedor@gmail.com", true))
    );

    Beneficiario hogarSantaRita = new Beneficiario(
        "Hogar Santa Rita",
        "Av. Santa Fe 3200", // Palermo / Barrio Norte
        List.of(new CorreoDeContacto("santarita@gmail.com", true))
    );
    Beneficiario merenderoRayito = new Beneficiario(
        "Merendero Rayito de Sol",
        "Av. Cabildo 2100", // Belgrano
        List.of(new CorreoDeContacto("rayitodesol@gmail.com", true))
    );
    Beneficiario fundacionAbrazo = new Beneficiario(
        "Fundación Abrazo",
        "Av. Almirante Brown 1200", // La Boca
        List.of(new CorreoDeContacto("abrazo@gmail.com", true))
    );
    Beneficiario comedorEsperanza = new Beneficiario(
        "Comedor Nueva Esperanza",
        "Av. Nazca 1800", // Villa Santa Rita / Flores
        List.of(new CorreoDeContacto("esperanza@gmail.com", true))
    );
    Beneficiario centroComunitario = new Beneficiario(
        "Centro Comunitario El Ceibo",
        "Av. Triunvirato 4500", // Villa Urquiza
        List.of(new CorreoDeContacto("elceibo@gmail.com", true))
    );

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

    Bien arrozBien = new Perecedero("Arroz largo fino", 15f, UnidadMedida.KILOGRAMOS, null, arroz, LocalDate.now().plusMonths(8));
    Bien aceiteBien = new Perecedero("Aceite de girasol", 12f, UnidadMedida.LITROS, null, aceite, LocalDate.now().plusMonths(10));

    // Bienes para las entregas históricas
    Bien lecheHist = new Perecedero("Leche en polvo fortificada", 25f, UnidadMedida.KILOGRAMOS, null, lacteos, LocalDate.now().plusMonths(5));
    Bien panialesHist = new NoPerecedero("Pañales descartables talle G", 40f, UnidadMedida.UNIDADES, null, paniales, true);
    Bien frazadasHist = new NoPerecedero("Frazadas polares", 18f, UnidadMedida.UNIDADES, null, frazadas, true);
    Bien legumbresHist = new Perecedero("Lentejas secas", 30f, UnidadMedida.KILOGRAMOS, null, legumbres, LocalDate.now().plusMonths(9));
    Bien limpiezaHist = new NoPerecedero("Bidones de lavandina", 15f, UnidadMedida.LITROS, null, lavandina, true);
    Bien sillasHist = new NoPerecedero("Sillas plásticas reforzadas", 12f, UnidadMedida.UNIDADES, null, sillas, true);

    Bien fideoHist = new Perecedero("Fideos tirabuzón", 20f, UnidadMedida.UNIDADES, null, pastas, LocalDate.now().plusMonths(6));
    Bien aceiteHist = new Perecedero("Aceite mezcla", 10f, UnidadMedida.LITROS, null, aceite, LocalDate.now().plusMonths(10));

    // Donaciones históricas (IDs 9L a 14L)
    Donacion donacion9 = new Donacion(List.of(lecheHist, panialesHist), List.of(juan));
    Donacion donacion10 = new Donacion(List.of(frazadasHist), List.of(juana));
    Donacion donacion11 = new Donacion(List.of(legumbresHist), List.of(juan, juana));
    Donacion donacion12 = new Donacion(List.of(limpiezaHist), List.of(juana));
    Donacion donacion13 = new Donacion(List.of(sillasHist), List.of(juan));
    Donacion donacion14 = new Donacion(List.of(fideoHist, aceiteHist), List.of(juana));

    // Completamos el ciclo de vida y pisamos la fecha con fechas viejas
    completarEntregaHistorica(donacion9, hogarSantaRita, LocalDateTime.of(2026, 10, 12, 14, 30));
    completarEntregaHistorica(donacion10, merenderoRayito, LocalDateTime.of(2026, 10, 18, 11, 15));
    completarEntregaHistorica(donacion11, fundacionAbrazo, LocalDateTime.of(2026, 10, 25, 16, 45));
    completarEntregaHistorica(donacion12, comedorEsperanza, LocalDateTime.of(2026, 11, 2, 10, 20));
    completarEntregaHistorica(donacion13, centroComunitario, LocalDateTime.of(2026, 11, 8, 15, 10));
    completarEntregaHistorica(donacion14, hogarSantaRita, LocalDateTime.of(2026, 11, 14, 17, 0));

    // Necesidades

    Necesidad necesidadLecheRecurrente = Necesidad.crearNecesidadRecurrente("Leche para el comedor", lacteos, UnidadMedida.LITROS, 50, Frecuencia.MENSUAL);
    Necesidad necesidadAbrigo = Necesidad.crearNecesidadExtraordinaria("Abrigos para invierno", abrigos, UnidadMedida.UNIDADES, 20);
    //Donaciones
    Donacion donacion1 = new Donacion(List.of(fideo), List.of(juan)); // ID 1
    Donacion donacion2 = new Donacion(List.of(leche), List.of(juan)); // ID 2
    Donacion donacion3 = new Donacion(List.of(manzana), List.of(juana)); // ID 3 -> EN CAMINO (Juana -> Comedor America)
    Donacion donacion4 = new Donacion(List.of(campera), List.of(juana)); // ID 4 -> ENTREGADA (Juana -> Comedor America)
    Donacion donacion5 = new Donacion(List.of(frazada), List.of(juana)); // ID 5 -> Ranking
    Donacion donacion6 = new Donacion(List.of(frazada2, campera2), List.of(juan, juana)); // ID 6 -> Ranking
    Donacion donacion7 = new Donacion(List.of(arrozBien), List.of(juan)); // ID 7 -> EN CAMINO (Juan -> Comedor Los Pibes)
    Donacion donacion8 = new Donacion(List.of(aceiteBien), List.of(juan)); // ID 8 -> ENTREGADA (Juan -> Comedor Los Pibes)
    //Rankings
    Ranking ranking1 = new Ranking(donacion5, List.of(comedorAmerica, comedorPibes));
    Ranking ranking2 = new Ranking(donacion6, List.of(comedorAmerica, comedorPibes));

    // Acciones
    donacion1.asignarA(comedorAmerica);

    donacion2.asignarA(comedorAmerica);
    donacion2.listaParaEntregar();

    // En camino (para Seguimiento de Juana y Comedor America)
    donacion3.asignarA(comedorAmerica);
    donacion3.listaParaEntregar();
    donacion3.enCamino();

    // Entregada (para Mapa Público en Villa Luro)
    donacion4.asignarA(comedorAmerica);
    donacion4.listaParaEntregar();
    donacion4.enCamino();
    donacion4.entregada();

    // En camino (para Seguimiento de Juan y Comedor Los Pibes)
    donacion7.asignarA(comedorPibes);
    donacion7.listaParaEntregar();
    donacion7.enCamino();

    // Entregada (para Mapa Público en Caballito)
    donacion8.asignarA(comedorPibes);
    donacion8.listaParaEntregar();
    donacion8.enCamino();
    donacion8.entregada();

    comedorAmerica.agregarNecesidad(necesidadLecheRecurrente);
    comedorAmerica.agregarNecesidad(necesidadAbrigo);

    //Transacción
    try {
      beginTransaction();
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
      repoRankings.guardar(ranking1);
      repoRankings.guardar(ranking2);
      repoBeneficiarios.guardar(hogarSantaRita);
      repoBeneficiarios.guardar(merenderoRayito);
      repoBeneficiarios.guardar(fundacionAbrazo);
      repoBeneficiarios.guardar(comedorEsperanza);
      repoBeneficiarios.guardar(centroComunitario);

      repoDonaciones.guardar(donacion1);  // ID 1L
      repoDonaciones.guardar(donacion2);  // ID 2L
      repoDonaciones.guardar(donacion3);  // ID 3L -> EN CAMINO (sincronizada con Logística)
      repoDonaciones.guardar(donacion4);  // ID 4L -> ENTREGADA
      repoDonaciones.guardar(donacion5);  // ID 5L
      repoDonaciones.guardar(donacion6);  // ID 6L
      repoDonaciones.guardar(donacion7);  // ID 7L -> EN CAMINO (sincronizada con Logística)
      repoDonaciones.guardar(donacion8);  // ID 8L -> ENTREGADA
      repoDonaciones.guardar(donacion9);  // ID 9L -> ENTREGADA (histórica)
      repoDonaciones.guardar(donacion10); // ID 10L -> ENTREGADA (histórica)
      repoDonaciones.guardar(donacion11); // ID 11L -> ENTREGADA (histórica)
      repoDonaciones.guardar(donacion12); // ID 12L -> ENTREGADA (histórica)
      repoDonaciones.guardar(donacion13); // ID 13L -> ENTREGADA (histórica)
      repoDonaciones.guardar(donacion14); // ID 14L -> ENTREGADA (histórica)
      commitTransaction();
      System.out.println("Datos iniciales cargados con éxito.");
    } catch (Exception e) {
      rollbackTransaction();
      System.err.println("Error al cargar los datos iniciales. Se canceló la transacción.");
      e.printStackTrace();
    }

  }

  //Auxiliar
  private void completarEntregaHistorica(Donacion donacion, Beneficiario destino, LocalDateTime fechaEntrega) {
    donacion.asignarA(destino);
    donacion.listaParaEntregar();
    donacion.enCamino();
    donacion.entregada();

    // Buscamos el último estado (ENTREGADA) y le pisamos la fecha actual por la histórica
    List<EstadoDonacion> historial = donacion.getHistorialEstados();
    if (historial != null && !historial.isEmpty()) {
      EstadoDonacion ultimoEstado = historial.get(historial.size() - 1);
      ultimoEstado.setFecha(fechaEntrega);
    }
  }

}


